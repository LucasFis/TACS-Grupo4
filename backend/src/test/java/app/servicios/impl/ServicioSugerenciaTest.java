package app.servicios.impl;

import app.MongoTestBase;
import app.dto.filtros.SugerenciasFiltro;
import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Seleccion;
import app.model.entities.Sugerencia;
import app.model.entities.Usuario;
import app.repositories.impl.campos.CamposPerfil;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSugerencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ServicioSugerenciaTest extends MongoTestBase {

  private ServicioSugerencia service;
  private Perfil usuario;
  private Perfil otro;
  private Figurita messi;

  @Mock
  private ServicioJwt jwt;

  @BeforeEach
  void setUp() {
    service = new ServicioSugerencia(repositorioSugerencias, repositorioPerfiles);
    Usuario user = new Usuario("u-1", Rol.USUARIO, "lucas", "fiscella");
    Coleccion colec = new Coleccion("c-1");
    usuario = Perfil.builder()
        .id("1").usuario(user).nombre("Lucas")
        .coleccion(colec)
        .mediosDeContacto(telegram("@lucas"))
        .build();
    repositorioColecciones.guardar(colec);
    repositorioUsuarios.guardar(user);

    user = new Usuario("u-2", Rol.USUARIO, "lucas", "fiscella");
    colec = new Coleccion("c-2");
    otro = Perfil.builder()
        .id("2").usuario(user).nombre("Sofía")
        .coleccion(colec)
        .mediosDeContacto(telegram("@sofía"))
        .build();
    repositorioColecciones.guardar(colec);
    repositorioUsuarios.guardar(user);
    repositorioPerfiles.guardar(usuario);
    repositorioPerfiles.guardar(otro);

    messi = Figurita.builder()
        .id("ARG-10")
        .numero(10)
        .jugador("Messi")
        .seleccion(Seleccion.ARGENTINA)
        .build();
    repositorioFiguritas.guardar(messi);
  }

  private List<MedioDeContacto> telegram(String numero) {
    return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
  }

  private void generarSugerencias() {
    this.repositorioSugerencias.eliminacionProgramada();

    List<Perfil> perfiles = this.repositorioPerfiles.buscarTodos(new CamposPerfil(false));
    perfiles.forEach(perfil -> {

      List<Sugerencia> sugerencias = this.repositorioSugerencias.generarSugerencias(perfil);
      this.repositorioSugerencias.guardar(sugerencias);
    });
  }

  @Test
  void obtenerSugerencias_conCoincidencias_retornaSugerencias() {
    Figurita diMaria = Figurita.builder()
        .id("ARG-11")
        .numero(11)
        .jugador("Di María")
        .seleccion(Seleccion.ARGENTINA)
        .build();
    repositorioFiguritas.guardar(diMaria);
    usuario.getColeccion().agregarFaltante(messi);
    usuario.getColeccion().getRepetidas().add(new FiguritaIntercambiable(diMaria, 2, List.of(MetodoIntercambio.INTERCAMBIO)));
    repositorioColecciones.guardar(usuario.getColeccion());

    Coleccion coleccionOtro = new Coleccion("c-3");
    coleccionOtro.getRepetidas().add(new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.INTERCAMBIO)));
    coleccionOtro.getFaltantes().add(diMaria);

    repositorioColecciones.guardar(coleccionOtro);

    Usuario user = new Usuario("u-3", Rol.USUARIO, "lucas", "fiscella");
    repositorioUsuarios.guardar(user);
    Perfil otroConMessi = Perfil.builder()
        .id("3").usuario(user).nombre("Juan")
        .coleccion(coleccionOtro)
        .mediosDeContacto(telegram("@juan"))
        .build();
    repositorioPerfiles.guardar(otroConMessi);

    when(jwt.getPerfilId(any())).thenReturn("1");

    this.generarSugerencias();

    var resultado = service.obtenerSugerencias("1", new SugerenciasFiltro(1, 10));

    assertEquals(1, resultado.contenido().size());
  }

  @Test
  void obtenerSugerencias_sinCoincidencias_retornaListaVacia() {
    Figurita messi = Figurita.builder()
        .id("ARG-10")
        .numero(10)
        .jugador("Messi")
        .seleccion(Seleccion.ARGENTINA)
        .build();
    usuario.getColeccion().getFaltantes().add(messi);

    when(jwt.getPerfilId(any())).thenReturn("1");

    var resultado = service.obtenerSugerencias("1", new SugerenciasFiltro(1, 10));

    assertEquals(0, resultado.contenido().size());
  }

  @Test
  void alternarFavorito_cambiaEstadoAFavorito() {
    Sugerencia sugerencia = Sugerencia.builder()
        .id("sug-001")
        .autor(usuario)
        .sugerido(otro)
        .figuritasSugeridas(List.of(messi))
        .figuritasNecesarias(List.of(messi))
        .build();
    repositorioSugerencias.guardar(sugerencia);

    service.alternarFavorito("sug-001", usuario.getId());

    Sugerencia resultado = repositorioSugerencias.buscarPorId("sug-001");
    assertEquals(true, resultado.getFavorito());
  }

  @Test
  void alternarFavorito_cambiaEstadoDeFavoritoANoFavorito() {
    Sugerencia sugerencia = Sugerencia.builder()
        .id("sug-002")
        .autor(usuario)
        .sugerido(otro)
        .figuritasSugeridas(List.of(messi))
        .figuritasNecesarias(List.of(messi))
        .favorito(true)
        .build();
    repositorioSugerencias.guardar(sugerencia);

    service.alternarFavorito("sug-002", usuario.getId());

    Sugerencia resultado = repositorioSugerencias.buscarPorId("sug-002");
    assertEquals(false, resultado.getFavorito());
  }

  @Test
  void alternarFavorito_sugerenciaInexistente_lanzaExcepcion() {
    assertThrows(NotFoundException.class, () ->
        service.alternarFavorito("no-existe", usuario.getId())
    );
  }

  @Test
  void obtenerSugerencias_soloRetornaSugerenciasDelPerfil() {
    Figurita diMaria = Figurita.builder()
        .id("ARG-11")
        .numero(11)
        .jugador("Di María")
        .seleccion(Seleccion.ARGENTINA)
        .build();
    repositorioFiguritas.guardar(diMaria);

    Sugerencia sugerencia1 = Sugerencia.builder()
        .id("sug-003")
        .autor(usuario)
        .sugerido(otro)
        .figuritasSugeridas(List.of(messi))
        .figuritasNecesarias(List.of(diMaria))
        .build();

    Sugerencia sugerencia2 = Sugerencia.builder()
        .id("sug-004")
        .autor(otro)
        .sugerido(usuario)
        .figuritasSugeridas(List.of(diMaria))
        .figuritasNecesarias(List.of(messi))
        .build();

    repositorioSugerencias.guardar(sugerencia1);
    repositorioSugerencias.guardar(sugerencia2);

    var resultado = service.obtenerSugerencias(usuario.getId(), new SugerenciasFiltro(0, 10));

    assertEquals(1, resultado.contenido().size());
  }

  @Test
  void obtenerSugerencias_paginacionCorrecta() {
    for (int i = 0; i < 5; i++) {
      Sugerencia s = Sugerencia.builder()
          .id("sug-pag-" + i)
          .autor(usuario)
          .sugerido(otro)
          .figuritasSugeridas(List.of(messi))
          .figuritasNecesarias(List.of(messi))
          .build();
      repositorioSugerencias.guardar(s);
    }

    var resultado = service.obtenerSugerencias(usuario.getId(), new SugerenciasFiltro(0, 2));

    assertEquals(2, resultado.contenido().size());
    assertEquals(5, resultado.cantidadDeElementos());
    assertEquals(3, resultado.cantidadDePaginas());
  }
}
