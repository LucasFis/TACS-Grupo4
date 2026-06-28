package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.MongoTestBase;
import app.dto.FiguritaIntercambiableDto;
import app.dto.filtros.SugerenciasFiltro;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import app.servicios.ServicioJwt;
import app.servicios.ServicioNotificacion;
import app.servicios.ServicioPerfil;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ServicioPerfilTest extends MongoTestBase {

  private ServicioPerfil service;
  private Perfil usuario;
  private Perfil otro;
  private Figurita messi;

  @Mock
  private ServicioJwt jwt;
  @Mock
  private ServicioNotificacion servicioNotificacion;

  @BeforeEach
  void setUp() {
    service = new ServicioPerfil(repositorioCalificacion, repositorioPerfiles, repositorioNotificaciones, servicioNotificacion, repositorioUsuarios, new SimpleMeterRegistry());

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

  private Propuesta propuesta(String id, Perfil autor, Perfil destino, EstadoProceso estado) {
    return new Propuesta(
        id, autor, destino, new ArrayList<>(), null,
        new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), estado))),new EstadoPropuesta(LocalDateTime.now(), estado));
  }


  @Test
  void agregarCalificacion_valida_guardaCalificacion() {
    Calificacion calificacion = new Calificacion(
        "c-0",
        otro,
        usuario,
        4,
        "Buen intercambio",
        "t-1",
        MetodoIntercambio.INTERCAMBIO
    );

    repositorioCalificacion.guardar(calificacion);
    repositorioPerfiles.guardar(usuario);

    assertDoesNotThrow(
        () -> service.agregarCalificacion(
            otro.getId(),
            usuario.getId(),
            2,
            "t-2",
            "Buen usuario",
            MetodoIntercambio.INTERCAMBIO
        ));

  }

  @Test
  void agregarCalificacion_yaCalificado_lanzaExcepcion() {

    Calificacion calificacion = new Calificacion(
        "c-0",
        otro,
        usuario,
        4,
        "Buen intercambio",
        "t-1",
        MetodoIntercambio.INTERCAMBIO
    );

    repositorioCalificacion.guardar(calificacion);
    usuario.agregarNuevaCalificacion(calificacion);
    repositorioPerfiles.guardar(usuario);

    assertThrows(BadRequestException.class,
        () -> service.agregarCalificacion(
            otro.getId(),
            usuario.getId(),
            2,
            "Tardó en responder",
            "t-1",
            MetodoIntercambio.INTERCAMBIO
        )
    );
  }

  @Test
  void agregarCalificacion_valorNegativo_lanzaExcepcion() {
    assertThrows(BadRequestException.class,
        () -> service.agregarCalificacion("2", "1", -1, "Malo", "t-1", MetodoIntercambio.INTERCAMBIO));
  }

  @Test
  void agregarCalificacion_valorCero_lanzaExcepcion() {
    assertThrows(BadRequestException.class,
        () -> service.agregarCalificacion("2", "1", 0, "Malo", "t-1", MetodoIntercambio.INTERCAMBIO));
  }

  @Test
  void agregarCalificacion_valorMayorACinco_lanzaExcepcion() {
    assertThrows(BadRequestException.class,
        () -> service.agregarCalificacion("2", "1", 6, "Excelente", "t-1", MetodoIntercambio.INTERCAMBIO));
  }

  @Test
  void agregarCalificacion_valorNulo_lanzaExcepcion() {
    assertThrows(BadRequestException.class,
        () -> service.agregarCalificacion("2", "1", null, "Sin valor", "t-1", MetodoIntercambio.INTERCAMBIO));
  }

  @Test
  void agregarCalificacion_valorLimiteMinimo_noLanzaExcepcion() {
    assertDoesNotThrow(() ->
        service.agregarCalificacion(
            otro.getId(),
            usuario.getId(),
            1,
            "Muy malo",
            "t-1",
            MetodoIntercambio.INTERCAMBIO
        )
    );
  }


  @Test
  void agregarCalificacion_valorLimiteMaximo_noLanzaExcepcion() {
    assertDoesNotThrow(() ->
        service.agregarCalificacion(
            otro.getId(),
            usuario.getId(),
            5,
            "Excelente",
            "t-1",
            MetodoIntercambio.INTERCAMBIO
        )
    );
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
  void obtenerFaltantes_usuarioExistente_retornaLista() {

    usuario.getColeccion().agregarFaltante(messi);
    repositorioColecciones.guardar(usuario.getColeccion());

    var resultado = service.obtenerFaltantes("u-1");

    assertEquals(1, resultado.size());
  }

  @Test
  void obtenerFaltantes_usuarioInexistente_lanzaNotFoundException() {

    assertThrows(NotFoundException.class,
        () -> service.obtenerFaltantes("u-99"));
  }

}