package app.repositories.impl;

import app.MongoTestBase;
import app.dto.filtros.FaltantesFiltro;
import app.dto.filtros.FiguritasFiltro;
import app.dto.filtros.RepetidasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.paginacion.Repetidas;
import app.exceptions.NotFoundException;
import app.model.entities.*;
import app.repositories.impl.campos.CamposColeccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositorioColeccionesTest extends MongoTestBase {

  Figurita messi;
  Figurita diMaria;
  Figurita dybala;

  @BeforeEach
  void setUp() {
    messi = Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build();
    diMaria = Figurita.builder().id("ARG-11").numero(11).jugador("Di maria").seleccion(Seleccion.ARGENTINA).build();
    dybala = Figurita.builder().id("ARG-21").numero(21).jugador("Dybala").seleccion(Seleccion.ARGENTINA).build();

    repositorioFiguritas.guardar(messi);
    repositorioFiguritas.guardar(diMaria);
    repositorioFiguritas.guardar(dybala);
  }

  @Test
  void findByIdNoEncuentraYtiraExcepcion() {
    Coleccion coleccion = new Coleccion();

    repositorioColecciones.guardar(coleccion);

    CamposColeccion sinCampos = new CamposColeccion(false, false);

    assertThrows(RuntimeException.class, () -> repositorioColecciones.buscarPorId("11", sinCampos));
  }

  @Test
  void findByIdNoCargaListas() {
    Coleccion coleccion = new Coleccion();
    coleccion.getFaltantes().add(messi);
    coleccion.agregarRepetida(new FiguritaIntercambiable(diMaria, 2, List.of(MetodoIntercambio.INTERCAMBIO)));

    repositorioColecciones.guardar(coleccion);

    coleccion = repositorioColecciones.buscarPorId(coleccion.getId(), new CamposColeccion(false, false));
    assertEquals(0, coleccion.getFaltantes().size());
    assertEquals(0, coleccion.getRepetidas().size());
    assertInstanceOf(List.class, coleccion.getFaltantes());
    assertInstanceOf(List.class, coleccion.getRepetidas());
  }

  @Test
  void findByIdCargaListaRepetidaYNoFaltante() {
    Coleccion coleccion = new Coleccion();
    coleccion.getFaltantes().add(messi);
    coleccion.agregarRepetida(new FiguritaIntercambiable(diMaria, 2, List.of(MetodoIntercambio.INTERCAMBIO)));

    repositorioColecciones.guardar(coleccion);

    coleccion = repositorioColecciones.buscarPorId(coleccion.getId(), new CamposColeccion(true, false));
    assertEquals(0, coleccion.getFaltantes().size());
    assertEquals(1, coleccion.getRepetidas().size());
    assertInstanceOf(List.class, coleccion.getFaltantes());
  }

  @Test
  void findByIdValido() {
    Coleccion coleccion = new Coleccion();

    repositorioColecciones.guardar(coleccion);


    assertEquals(coleccion.getId(), repositorioColecciones.buscarPorId(coleccion.getId(), new CamposColeccion(false, false)).getId());
  }

  @Test
  void buscarFaltantesDevuelveResultadosPaginados() {
    Coleccion coleccion = new Coleccion();

    coleccion.getFaltantes().addAll(List.of(
        messi,
        diMaria,
        dybala
    ));

    repositorioColecciones.guardar(coleccion);

    PaginaResultado<Figurita> dto = repositorioColecciones.buscarFaltantes(
        coleccion.getId(),
        new FaltantesFiltro(2, 1)
    );

    assertEquals(3, dto.cantidadDeElementos());
    assertEquals(1, dto.numero());
    assertEquals(2, dto.cantidadDePaginas());
    assertEquals(2, dto.contenido().size());
  }

  @Test
  void buscarFaltantesSegundaPagina() {
    Coleccion coleccion = new Coleccion();

    coleccion.getFaltantes().addAll(List.of(
        messi,
        diMaria,
        dybala
    ));

    repositorioColecciones.guardar(coleccion);

    PaginaResultado dto = repositorioColecciones.buscarFaltantes(
        coleccion.getId(),
        new FaltantesFiltro(2, 2)
    );

    assertEquals(1, dto.contenido().size());
    assertEquals(2, dto.numero());
  }

  @Test
  void buscarRepetidasSinFiltroDevuelveTodo() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().addAll(List.of(
        FiguritaIntercambiable.builder().figurita(messi).cantidadExistente(2).metodos(List.of(MetodoIntercambio.SUBASTA)).build(),
        FiguritaIntercambiable.builder().figurita(diMaria).cantidadExistente(3).metodos(List.of(MetodoIntercambio.INTERCAMBIO)).build(),
        FiguritaIntercambiable.builder().figurita(dybala).cantidadExistente(1).metodos(List.of(MetodoIntercambio.SUBASTA)).build()
    ));

    repositorioColecciones.guardar(coleccion);

    Repetidas dto = repositorioColecciones.buscarRepetidas(
        coleccion.getId(),
        new RepetidasFiltro(null, null, 10, 1),
        null

    );

    assertEquals(3, dto.getPublicadas());
    assertEquals(6, dto.getDisponibles());
    assertEquals(3, dto.getData().cantidadDeElementos());
    assertEquals(3, dto.getData().contenido().size());
  }

  @Test
  void buscarRepetidasFiltraPorSubasta() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.SUBASTA)),
        new FiguritaIntercambiable(diMaria, 3, List.of(MetodoIntercambio.INTERCAMBIO)),
        new FiguritaIntercambiable(dybala, 1, List.of(MetodoIntercambio.SUBASTA))
    ));

    repositorioColecciones.guardar(coleccion);

    Repetidas dto = repositorioColecciones.buscarRepetidas(
        coleccion.getId(),
        new RepetidasFiltro(MetodoIntercambio.SUBASTA, null, 10, 1),
        null
    );

    assertEquals(2, dto.getData().cantidadDeElementos());
    assertEquals(2, dto.getData().contenido().size());
  }

  @Test
  void buscarRepetidasFiltraPorIntercambio() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.SUBASTA)),
        new FiguritaIntercambiable(diMaria, 3, List.of(MetodoIntercambio.INTERCAMBIO)),
        new FiguritaIntercambiable(dybala, 1, List.of(MetodoIntercambio.SUBASTA))
    ));

    repositorioColecciones.guardar(coleccion);

    Repetidas<FiguritaIntercambiable> dto = repositorioColecciones.buscarRepetidas(
        coleccion.getId(),
        new RepetidasFiltro(MetodoIntercambio.INTERCAMBIO, null, 10, 1),
        null
    );

    assertEquals(1, dto.getData().cantidadDeElementos());
    assertEquals(1, dto.getData().contenido().size());
  }

  @Test
  void buscarRepetidasRespetaPaginacion() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.SUBASTA)),
        new FiguritaIntercambiable(diMaria, 3, List.of(MetodoIntercambio.INTERCAMBIO)),
        new FiguritaIntercambiable(dybala, 1, List.of(MetodoIntercambio.SUBASTA))
    ));

    repositorioColecciones.guardar(coleccion);

    Repetidas dto = repositorioColecciones.buscarRepetidas(
        coleccion.getId(),
        new RepetidasFiltro(null, null, 2, 2),
        null
    );

    assertEquals(1, dto.getData().contenido().size());
    assertEquals(2, dto.getData().numero());
    assertEquals(2, dto.getData().cantidadDePaginas());
  }

  @Test
  void buscarIntercambiablesConFiltrosPorTipo() {
    Coleccion coleccion = new Coleccion("10");

    coleccion.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(
            messi,
            2,
            List.of(MetodoIntercambio.SUBASTA)
        ),
        new FiguritaIntercambiable(
            diMaria,
            1,
            List.of(MetodoIntercambio.INTERCAMBIO)
        )
    ));

    repositorioColecciones.guardar(coleccion);

    var resultado =
        repositorioColecciones.buscarIntercambiablesConFiltros(
            new FiguritasFiltro(
                null,
                null,
                null,
                null,
                List.of(MetodoIntercambio.SUBASTA),
                null,
                null
            ),
            1,
            10
        );

    assertEquals(1, resultado.contenido().size());
    assertEquals("Messi",
        resultado.contenido().get(0).figurita().getFigurita().getJugador());
  }

  @Test
  void buscarRepetidasFiltraPorCoincidenciaConFaltantesDeOtroPerfil() {
    // Colección del perfil logueado con repetidas
    Coleccion colLogueado = new Coleccion();
    colLogueado.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.INTERCAMBIO)),
        new FiguritaIntercambiable(diMaria, 3, List.of(MetodoIntercambio.INTERCAMBIO)),
        new FiguritaIntercambiable(dybala, 1, List.of(MetodoIntercambio.INTERCAMBIO))
    ));
    repositorioColecciones.guardar(colLogueado);

    // Colección del otro perfil con solo messi como faltante
    Coleccion colOtroPerfil = new Coleccion();
    colOtroPerfil.getFaltantes().add(messi);
    repositorioColecciones.guardar(colOtroPerfil);

    Repetidas<FiguritaIntercambiable> dto = repositorioColecciones.buscarRepetidas(
        colLogueado.getId(),
        new RepetidasFiltro(null, null, 10, 1),
        colOtroPerfil.getId()
    );

    assertEquals(1, dto.getData().cantidadDeElementos());
    assertEquals(1, dto.getData().contenido().size());
    assertEquals("Messi", dto.getData().contenido().get(0).getFigurita().getJugador());
  }

  @Test
  void buscarRepetidasConFaltantesDeOtroPerfilSinCoincidencias() {
    Coleccion colLogueado = new Coleccion();
    colLogueado.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.INTERCAMBIO))
    ));
    repositorioColecciones.guardar(colLogueado);

    // El otro perfil tiene faltantes que no coinciden con ninguna repetida
    Coleccion colOtroPerfil = new Coleccion();
    colOtroPerfil.getFaltantes().add(dybala);
    repositorioColecciones.guardar(colOtroPerfil);

    Repetidas<FiguritaIntercambiable> dto = repositorioColecciones.buscarRepetidas(
        colLogueado.getId(),
        new RepetidasFiltro(null, null, 10, 1),
        colOtroPerfil.getId()
    );

    assertEquals(0, dto.getData().cantidadDeElementos());
    assertEquals(0, dto.getData().contenido().size());
  }

  @Test
  void buscarRepetidasCombinaFiltroMetodoYCoincidenciaFaltantes() {
    Coleccion colLogueado = new Coleccion();
    colLogueado.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.SUBASTA)),
        new FiguritaIntercambiable(diMaria, 3, List.of(MetodoIntercambio.INTERCAMBIO))
        // ambas son faltantes del otro perfil, pero solo messi es SUBASTA
    ));
    repositorioColecciones.guardar(colLogueado);

    Coleccion colOtroPerfil = new Coleccion();
    colOtroPerfil.getFaltantes().addAll(List.of(messi, diMaria));
    repositorioColecciones.guardar(colOtroPerfil);

    Repetidas<FiguritaIntercambiable> dto = repositorioColecciones.buscarRepetidas(
        colLogueado.getId(),
        new RepetidasFiltro(MetodoIntercambio.SUBASTA, null, 10, 1),
        colOtroPerfil.getId()
    );

    assertEquals(1, dto.getData().cantidadDeElementos());
    assertEquals("Messi", dto.getData().contenido().get(0).getFigurita().getJugador());
  }

  @Test
  void buscarRepetida_retornaLaRepetidaCorrecta() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(messi, 2, List.of(MetodoIntercambio.SUBASTA)),
        new FiguritaIntercambiable(diMaria, 5, List.of(MetodoIntercambio.INTERCAMBIO))
    ));

    repositorioColecciones.guardar(coleccion);

    FiguritaIntercambiable resultado =
        repositorioColecciones.buscarRepetida(
            coleccion.getId(),
            "ARG-11"
        );

    assertNotNull(resultado);
    assertEquals("ARG-11", resultado.getFigurita().getId());
    assertEquals(5, resultado.getCantidadExistente());
  }

  @Test
  void buscarRepetida_lanzaExcepcion_siNoExiste() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().add(
        new FiguritaIntercambiable(
            messi,
            2,
            List.of(MetodoIntercambio.INTERCAMBIO)
        )
    );

    repositorioColecciones.guardar(coleccion);

    assertThrows(
        NotFoundException.class,
        () -> repositorioColecciones.buscarRepetida(
            coleccion.getId(),
            "ARG-21"
        )
    );
  }

  @Test
  void actualizarRepetida_actualizaCantidadYMetodos() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().add(
        new FiguritaIntercambiable(
            messi,
            2,
            List.of(MetodoIntercambio.INTERCAMBIO)
        )
    );

    repositorioColecciones.guardar(coleccion);

    FiguritaIntercambiable actualizada =
        new FiguritaIntercambiable(
            messi,
            7,
            List.of(MetodoIntercambio.SUBASTA)
        );

    repositorioColecciones.actualizarRepetida(
        coleccion.getId(),
        "ARG-10",
        actualizada
    );

    FiguritaIntercambiable resultado =
        repositorioColecciones.buscarRepetida(
            coleccion.getId(),
            "ARG-10"
        );

    assertEquals(7, resultado.getCantidadExistente());
    assertEquals(1, resultado.getMetodos().size());
    assertEquals(MetodoIntercambio.SUBASTA, resultado.getMetodos().get(0));
  }

  @Test
  void actualizarRepetida_lanzaExcepcion_siNoExiste() {
    Coleccion coleccion = new Coleccion();
    repositorioColecciones.guardar(coleccion);

    FiguritaIntercambiable repetida =
        new FiguritaIntercambiable(
            messi,
            5,
            List.of(MetodoIntercambio.SUBASTA)
        );

    assertThrows(
        NotFoundException.class,
        () -> repositorioColecciones.actualizarRepetida(
            coleccion.getId(),
            "ARG-10",
            repetida
        )
    );
  }

  @Test
  void actualizarRepetida_noModificaOtrasRepetidas() {
    Coleccion coleccion = new Coleccion();

    coleccion.getRepetidas().addAll(List.of(
        new FiguritaIntercambiable(
            messi,
            2,
            List.of(MetodoIntercambio.INTERCAMBIO)
        ),
        new FiguritaIntercambiable(
            diMaria,
            3,
            List.of(MetodoIntercambio.SUBASTA)
        )
    ));

    repositorioColecciones.guardar(coleccion);

    FiguritaIntercambiable actualizada =
        new FiguritaIntercambiable(
            messi,
            10,
            List.of(MetodoIntercambio.SUBASTA)
        );

    repositorioColecciones.actualizarRepetida(
        coleccion.getId(),
        "ARG-10",
        actualizada
    );

    FiguritaIntercambiable messiActualizada =
        repositorioColecciones.buscarRepetida(
            coleccion.getId(),
            "ARG-10"
        );

    FiguritaIntercambiable diMariaSinCambios =
        repositorioColecciones.buscarRepetida(
            coleccion.getId(),
            "ARG-11"
        );

    assertEquals(10, messiActualizada.getCantidadExistente());

    assertEquals(3, diMariaSinCambios.getCantidadExistente());
    assertEquals(
        MetodoIntercambio.SUBASTA,
        diMariaSinCambios.getMetodos().get(0)
    );
  }

  @Test
  void contarRepetidas_modalidadExclusiva_noContabilizaAmbos() {
    Coleccion coleccion = new Coleccion();
    coleccion.agregarRepetida(new FiguritaIntercambiable(messi, 1, List.of(MetodoIntercambio.INTERCAMBIO)));
    coleccion.agregarRepetida(new FiguritaIntercambiable(diMaria, 1, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA)));
    repositorioColecciones.guardar(coleccion);

    long soloIntercambio = repositorioColecciones.contarRepetidas(List.of(MetodoIntercambio.INTERCAMBIO));
    long soloSubasta = repositorioColecciones.contarRepetidas(List.of(MetodoIntercambio.SUBASTA));
    long ambos = repositorioColecciones.contarRepetidas(List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA));

    assertEquals(1, soloIntercambio);
    assertEquals(0, soloSubasta);
    assertEquals(1, ambos);
  }
}
