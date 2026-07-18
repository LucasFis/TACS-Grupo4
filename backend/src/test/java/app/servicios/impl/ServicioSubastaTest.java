package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.MongoTestBase;
import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.EditarOfertaRequest;
import app.dto.subasta.MiSubastaActivaDto;
import app.dto.subasta.MiSubastaFinalizadaDto;
import app.dto.subasta.SubastaParticipoDto;

import app.exceptions.BadRequestException;
import app.model.entities.*;
import app.repositories.impl.campos.CamposColeccion;
import java.time.LocalDateTime;
import java.util.List;

import app.repositories.impl.campos.CamposSubasta;
import app.servicios.ServicioSubasta;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ServicioSubastaTest extends MongoTestBase {

  @Autowired
  private ServicioSubasta service;

  private Perfil lucas;
  private Perfil sofia;
  private Figurita messi;

  private List<MedioDeContacto> telegram(String numero) {
    return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
  }

  @BeforeEach
  void setUp() {
    messi = Figurita.builder()
        .id("ARG-10")
        .numero(10)
        .jugador("Messi")
        .seleccion(Seleccion.ARGENTINA)
        .build();
    repositorioFiguritas.guardar(messi);

    Coleccion coleccionMessiFaltante = new Coleccion("c-1");
    repositorioColecciones.guardar(coleccionMessiFaltante);
    coleccionMessiFaltante.getFaltantes().add(messi);

    Usuario user = new Usuario("u-1", Rol.USUARIO, "lucas", "fiscella");
    lucas = Perfil.builder()
        .id("1")
        .usuario(user)
        .nombre("Lucas")
        .coleccion(coleccionMessiFaltante)
        .mediosDeContacto(telegram("@lucas"))
        .build();

    repositorioUsuarios.guardar(user);
    repositorioColecciones.guardar(coleccionMessiFaltante,  new CamposColeccion(false, true));
    repositorioPerfiles.guardar(lucas);

    Coleccion coleccionRepetidos = new Coleccion("c-2");
    coleccionRepetidos.getRepetidas().add(new FiguritaIntercambiable(messi, 1, List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA)));

    user = new Usuario("u-2", Rol.USUARIO, "sofia", "ape");
    sofia = Perfil.builder()
        .id("2")
        .usuario(user)
        .nombre("Sofía")
        .coleccion(coleccionRepetidos)
        .mediosDeContacto(telegram("@sofia"))
        .build();

    repositorioUsuarios.guardar(user);
    repositorioColecciones.guardar(coleccionRepetidos);
    repositorioPerfiles.guardar(sofia);
  }

  @Test
  void crearSubastaNotificaUsuarios() {
    service.crearSubasta("2", "ARG-10", 30, List.of(), 0);
    assertEquals(1, repositorioNotificaciones.buscarPorPerfil(lucas).size());
  }

  @Test
  void crearSubastaNoNotificaUsuarios() {
    Figurita diMaria = Figurita.builder()
        .id("ARG-11")
        .numero(11)
        .jugador("Di María")
        .seleccion(Seleccion.ARGENTINA)
        .build();
    repositorioFiguritas.guardar(diMaria);

    service.crearSubasta("2", "ARG-11", 30, List.of(), 0);
    assertEquals(0, repositorioNotificaciones.buscarPorPerfil(lucas).size());
  }

  @Test
  void ofertarEnSubastaCerrada_lanzaExcepcion() {
    Subasta subastaCerrada = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(3))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi).build();

    repositorioSubastas.guardar(subastaCerrada);

    assertThrows(BadRequestException.class,
        () -> service.ofertarEnSubasta(sofia.getId(), "s-1", List.of("ARG-10")));
  }

  @Test
  void ofertarEnSubastaConFiguritasDuplicadas_lanzaExcepcion() {
    Subasta subastaActiva = Subasta.builder()
        .id("s-2")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subastaActiva);

    assertThrows(BadRequestException.class,
        () -> service.ofertarEnSubasta(sofia.getId(), "s-2", List.of("ARG-10", "ARG-10")));
  }

  @Test
  void seleccionarOferta_marcaComoSeleccionada() {
    Subasta subasta = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi).build();

    Propuesta propuesta = Propuesta.builder()
        .id("o-1").autor(lucas)
        .destinatario(sofia)
        .figuritasOfrecidas(List.of())
        .figuritaBuscada(messi)
        .build();
    subasta.agregarOferta(propuesta);

    repositorioSubastas.guardar(subasta);

    service.seleccionarOferta(sofia.getId(), "s-1", "o-1");

    subasta = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, true));

    assertEquals(EstadoProceso.SELECCIONADO, buscarOfertaEn(subasta, propuesta.getId()).getEstadoActual().getValor());
  }

  @Test
  void seleccionarOferta_desseleccionaAnterior() {
    Subasta subasta = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi).build();

    Propuesta propuestaAnterior = Propuesta.builder()
        .id("o-1").autor(lucas)
        .destinatario(sofia)
        .figuritasOfrecidas(List.of())
        .figuritaBuscada(messi)
        .build();
    propuestaAnterior.seleccionar(sofia.getId());

    Propuesta propuestaNueva = Propuesta.builder()
        .id("o-2").autor(lucas)
        .destinatario(sofia)
        .figuritasOfrecidas(List.of())
        .figuritaBuscada(messi)
        .build();

    subasta.agregarOferta(propuestaAnterior);
    subasta.agregarOferta(propuestaNueva);

    repositorioSubastas.guardar(subasta);

    service.seleccionarOferta(sofia.getId(), "s-1", "o-2");

    subasta = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, true));

    assertEquals(EstadoProceso.PENDIENTE, buscarOfertaEn(subasta, propuestaAnterior.getId()).getEstadoActual().getValor());
    assertEquals(EstadoProceso.SELECCIONADO, buscarOfertaEn(subasta, propuestaNueva.getId()).getEstadoActual().getValor());
  }

  @Test
  void seleccionarOferta_subastaInactiva_lanzaExcepcion() {
    Subasta subastaCerrada = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(2))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi).build();

    repositorioSubastas.guardar(subastaCerrada);

    assertThrows(BadRequestException.class,
        () -> service.seleccionarOferta(sofia.getId(), "s-1", "o-1"));
  }

  @Test
  void seleccionarOferta_ofertaInexistente_lanzaExcepcion() {
    Subasta subasta = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi).build();

    repositorioSubastas.guardar(subasta);

    assertThrows(BadRequestException.class,
        () -> service.seleccionarOferta(sofia.getId(), "s-1", "inexistente"));
  }

  @Test
  void rechazarOferta_subastaInactiva_lanzaExcepcion() {
    Subasta subastaCerrada = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(2))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi).build();

    repositorioSubastas.guardar(subastaCerrada);

    assertThrows(BadRequestException.class,
        () -> service.rechazarOferta(sofia.getId(), "s-1", "o-1"));
  }

  @Test
  void rechazarOferta_ofertaInexistente_lanzaExcepcion() {
    Subasta subasta = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi).build();

    repositorioSubastas.guardar(subasta);

    assertThrows(BadRequestException.class,
        () -> service.rechazarOferta(sofia.getId(), "s-1", "inexistente"));
  }

  @Test
  void cancelarSubasta_subastaInactiva_lanzaExcepcion() {
    Subasta subastaCerrada = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(2))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi).build();

    repositorioSubastas.guardar(subastaCerrada);

    assertThrows(BadRequestException.class,
        () -> service.cancelarSubasta(sofia.getId(), "s-1"));
  }

  @Test
  void cerrarSubasta_subastaInactiva_lanzaExcepcion() {
    Subasta subastaCerrada = Subasta.builder()
        .id("s-1").autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(2))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi).build();

    repositorioSubastas.guardar(subastaCerrada);

    assertThrows(BadRequestException.class,
        () -> service.cerrarSubasta(sofia.getId(), "s-1"));
  }

  @Test
  void ofertarEnSubasta_agregaOfertaCorrectamente() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subasta);

    service.ofertarEnSubasta(lucas.getId(), "s-1", List.of("ARG-10"));

    subasta = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, true));

    assertEquals(1, subasta.getOfertas().size());
    assertEquals(lucas.getId(), subasta.getOfertas().get(0).getAutor().getId());
  }

  @Test
  void editarOferta_actualizaFiguritas() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    Propuesta propuesta = Propuesta.builder()
        .id("o-1")
        .autor(lucas)
        .destinatario(sofia)
        .figuritaBuscada(messi)
        .figuritasOfrecidas(List.of())
        .build();

    subasta.agregarOferta(propuesta);
    repositorioSubastas.guardar(subasta);

    service.editarOfertaEnSubasta(lucas.getId(), "s-1", "o-1", new EditarOfertaRequest(List.of("ARG-10")));

    subasta = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, true));

    assertEquals(1, buscarOfertaEn(subasta, "o-1").getFiguritasOfrecidas().size());
  }

  @Test
  void editarOferta_ofertaInexistente_lanzaExcepcion() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subasta);

    assertThrows(BadRequestException.class,
        () -> service.editarOfertaEnSubasta(lucas.getId(), "s-1", "inexistente", new EditarOfertaRequest(List.of("ARG-10"))));
  }

  @Test
  void cancelarOferta_marcaComoCancelada() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    Propuesta propuesta = Propuesta.builder()
        .id("o-1")
        .autor(lucas)
        .destinatario(sofia)
        .figuritaBuscada(messi)
        .figuritasOfrecidas(List.of())
        .build();

    subasta.agregarOferta(propuesta);
    repositorioSubastas.guardar(subasta);

    service.cancelarOferta(lucas.getId(), "s-1", "o-1");

    subasta = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, false));

    assertEquals(EstadoProceso.CANCELADO, buscarOfertaEn(subasta, "o-1").getEstadoActual().getValor());
  }

  @Test
  void cancelarOferta_ofertaInexistente_lanzaExcepcion() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subasta);

    assertThrows(BadRequestException.class,
        () -> service.cancelarOferta(lucas.getId(), "s-1", "inexistente"));
  }

  @Test
  void obtenerSubastas_misActivas_retornaMiSubastaActivaDto() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subasta);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, sofia.getId(), null, "ACTIVA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(sofia.getId(), filtros);

    assertEquals(1, resultado.contenido().size());
    assertInstanceOf(MiSubastaActivaDto.class, resultado.contenido().get(0));
    assertEquals("s-1", ((MiSubastaActivaDto) resultado.contenido().get(0)).getId());
  }

  @Test
  void obtenerSubastas_misFinalizadas_retornaMiSubastaFinalizadaDto() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(3))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subasta);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, sofia.getId(), null, "FINALIZADA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(sofia.getId(), filtros);

    assertEquals(1, resultado.contenido().size());
    assertInstanceOf(MiSubastaFinalizadaDto.class, resultado.contenido().get(0));
  }

  @Test
  void obtenerSubastas_misFinalizadas_conOfertaAceptada_tieneOfertaGanadora() {
    Propuesta propuesta = Propuesta.builder()
        .id("o-1")
        .autor(lucas)
        .destinatario(sofia)
        .figuritaBuscada(messi)
        .figuritasOfrecidas(List.of())
        .build();
    propuesta.aceptar(sofia.getId());

    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(3))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi)
        .ofertas(List.of(propuesta))
        .build();

    repositorioSubastas.guardar(subasta);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, sofia.getId(), null, "FINALIZADA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(sofia.getId(), filtros);

    MiSubastaFinalizadaDto dto = (MiSubastaFinalizadaDto) resultado.contenido().get(0);
    assertNotNull(dto.getOfertaGanadora());
    assertEquals("o-1", dto.getOfertaGanadora().getId());
  }

  @Test
  void obtenerSubastas_misFinalizadas_sinOfertaAceptada_ofertaGanadoraEsNull() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(3))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subasta);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, sofia.getId(), null, "FINALIZADA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(sofia.getId(), filtros);

    MiSubastaFinalizadaDto dto = (MiSubastaFinalizadaDto) resultado.contenido().get(0);
    assertNull(dto.getOfertaGanadora());
  }

  @Test
  void obtenerSubastas_misActivas_noRetornaFinalizadas() {
    Subasta activa = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    Subasta finalizada = Subasta.builder()
        .id("s-2")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusDays(3))
        .fechaCierre(LocalDateTime.now().minusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(activa);
    repositorioSubastas.guardar(finalizada);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, sofia.getId(), null, "ACTIVA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(sofia.getId(), filtros);

    assertEquals(1, resultado.contenido().size());
    assertEquals("s-1", ((MiSubastaActivaDto) resultado.contenido().get(0)).getId());
  }

  @Test
  void obtenerSubastas_conParticipante_retornaSubastaParticipoDto() {
    Propuesta propuesta = Propuesta.builder()
        .id("o-1")
        .autor(lucas)
        .destinatario(sofia)
        .figuritaBuscada(messi)
        .figuritasOfrecidas(List.of())
        .build();

    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .ofertas(List.of(propuesta))
        .build();

    repositorioSubastas.guardar(subasta);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, null, lucas.getId(), "ACTIVA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(lucas.getId(), filtros);

    assertEquals(1, resultado.contenido().size());
    assertInstanceOf(SubastaParticipoDto.class, resultado.contenido().get(0));
    SubastaParticipoDto dto = (SubastaParticipoDto) resultado.contenido().get(0);
    assertEquals("s-1", dto.getId());
    assertEquals("o-1", dto.getTuOferta().getId());
  }

  @Test
  void obtenerSubastas_conParticipante_ofertaCancelada_noRetornaSubasta() {
    Propuesta propuesta = Propuesta.builder()
        .id("o-1")
        .autor(lucas)
        .destinatario(sofia)
        .figuritaBuscada(messi)
        .figuritasOfrecidas(List.of())
        .build();
    propuesta.cancelar(lucas.getId());

    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .ofertas(List.of(propuesta))
        .build();

    repositorioSubastas.guardar(subasta);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, null, lucas.getId(), "ACTIVA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(lucas.getId(), filtros);

    assertTrue(resultado.contenido().isEmpty());
  }

  @Test
  void obtenerSubastas_conParticipante_sinOferta_noRetornaSubasta() {
    Subasta subasta = Subasta.builder()
        .id("s-1")
        .autor(sofia)
        .fechaInicio(LocalDateTime.now().minusHours(1))
        .fechaCierre(LocalDateTime.now().plusDays(1))
        .figuritaSubastada(messi)
        .build();

    repositorioSubastas.guardar(subasta);

    SubastasFiltro filtros = new SubastasFiltro(1, 10, null, lucas.getId(), "ACTIVA", null, null);

    PaginaResultado<?> resultado = service.obtenerSubastas(lucas.getId(), filtros);

    assertTrue(resultado.contenido().isEmpty());
  }

  private Propuesta buscarOfertaEn(Subasta subasta, String ofertaId) {
    return subasta.getOfertas().stream()
        .filter(o -> o.getId().equals(ofertaId))
        .findFirst()
        .orElseThrow();
  }
  @Nested
  class CerrarYCancelarSubasta {

    @BeforeEach
    void setUpColecciones() {
      Coleccion coleccionSofia = repositorioColecciones.buscarPorId("c-2", new CamposColeccion(true, false));
      coleccionSofia.getRepetidas().stream()
          .filter(r -> r.getFigurita().getId().equals(messi.getId()))
          .findFirst()
          .ifPresent(r -> r.setCantidadReservada(1));
      repositorioColecciones.guardar(coleccionSofia, new CamposColeccion(true, false));
    }

    @Test
    void cancelarSubasta_conOfertas_rechazaOfertasPendientes() {
      Propuesta propuesta = Propuesta.builder()
          .id("o-1").autor(lucas).destinatario(sofia)
          .figuritaBuscada(messi)
          .figuritasOfrecidas(List.of())
          .build();

      Subasta subasta = Subasta.builder()
          .id("s-1").autor(sofia)
          .fechaInicio(LocalDateTime.now().minusHours(1))
          .fechaCierre(LocalDateTime.now().plusDays(1))
          .figuritaSubastada(messi)
          .ofertas(List.of(propuesta))
          .build();

      repositorioSubastas.guardar(subasta);

      service.cancelarSubasta(sofia.getId(), "s-1");

      Subasta cancelada = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, true));
      assertEquals(EstadoProceso.RECHAZADO, buscarOfertaEn(cancelada, "o-1").getEstadoActual().getValor());
    }
    @Test
    void cerrarSubasta_sinOfertaSeleccionada_rechazaOfertasPendientes() {
      Propuesta propuesta = Propuesta.builder()
          .id("o-1").autor(lucas).destinatario(sofia)
          .figuritaBuscada(messi)
          .figuritasOfrecidas(List.of())
          .build();

      Subasta subasta = Subasta.builder()
          .id("s-1").autor(sofia)
          .fechaInicio(LocalDateTime.now().minusHours(1))
          .fechaCierre(LocalDateTime.now().plusDays(1))
          .figuritaSubastada(messi)
          .ofertas(List.of(propuesta))
          .build();

      repositorioSubastas.guardar(subasta);

      service.cerrarSubasta(sofia.getId(), "s-1");

      Subasta cerrada = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, true));
      assertEquals(EstadoProceso.RECHAZADO, buscarOfertaEn(cerrada, "o-1").getEstadoActual().getValor());
    }
    @Test
    void cerrarSubasta_conOfertaSeleccionada_aceptaGanador() {
      Propuesta propuesta = Propuesta.builder()
          .id("o-1").autor(lucas).destinatario(sofia)
          .figuritaBuscada(messi)
          .figuritasOfrecidas(List.of())
          .build();
      propuesta.seleccionar(sofia.getId());

      Subasta subasta = Subasta.builder()
          .id("s-1").autor(sofia)
          .fechaInicio(LocalDateTime.now().minusHours(1))
          .fechaCierre(LocalDateTime.now().plusDays(1))
          .figuritaSubastada(messi)
          .ofertas(List.of(propuesta))
          .build();

      repositorioSubastas.guardar(subasta);

      service.cerrarSubasta(sofia.getId(), "s-1");

      Subasta cerrada = repositorioSubastas.buscarPorId("s-1", new CamposSubasta(true, true));
      assertEquals(EstadoProceso.ACEPTADO, buscarOfertaEn(cerrada, "o-1").getEstadoActual().getValor());
    }
  }

  @Nested
  class ReservasColeccion {

    private Coleccion coleccionLucas;

    @BeforeEach
    void setUpRepetidas() {
      coleccionLucas = repositorioColecciones.buscarPorId("c-1", new CamposColeccion(true, true));
      coleccionLucas.getRepetidas().add(
          new FiguritaIntercambiable(messi, 2, 0, List.of(MetodoIntercambio.SUBASTA, MetodoIntercambio.INTERCAMBIO), lucas.getId())
      );
      repositorioColecciones.guardar(coleccionLucas, new CamposColeccion(true, true));
    }

    @Test
    void ofertarEnSubasta_reservaFiguritasDelAutor() {
      Subasta subasta = Subasta.builder()
          .id("s-1").autor(sofia)
          .fechaInicio(LocalDateTime.now().minusHours(1))
          .fechaCierre(LocalDateTime.now().plusDays(1))
          .figuritaSubastada(messi).build();
      repositorioSubastas.guardar(subasta);

      service.ofertarEnSubasta(lucas.getId(), "s-1", List.of("ARG-10"));

      Coleccion col = repositorioColecciones.buscarPorId("c-1", new CamposColeccion(true, true));
      FiguritaIntercambiable repetida = col.getRepetidas().stream()
          .filter(r -> r.getFigurita().getId().equals("ARG-10"))
          .findFirst().orElseThrow();

      assertEquals(1, repetida.getCantidadReservada());
    }
    @Test
    void ofertarEnSubasta_figuritaNoEsFaltante_lanzaExcepcion() {
      Figurita diMaria = Figurita.builder()
          .id("ARG-11")
          .numero(11)
          .jugador("Di María")
          .seleccion(Seleccion.ARGENTINA)
          .build();
      repositorioFiguritas.guardar(diMaria);

      Subasta subasta = Subasta.builder()
          .id("s-1").autor(sofia)
          .fechaInicio(LocalDateTime.now().minusHours(1))
          .fechaCierre(LocalDateTime.now().plusDays(1))
          .figuritaSubastada(diMaria).build();
      repositorioSubastas.guardar(subasta);

      assertThrows(BadRequestException.class,
          () -> service.ofertarEnSubasta(lucas.getId(), "s-1", List.of("ARG-10")));
    }


    @Test
    void cancelarOferta_liberaReservaDelAutor() {
      Propuesta propuesta = Propuesta.builder()
          .id("o-1").autor(lucas).destinatario(sofia)
          .figuritaBuscada(messi)
          .figuritasOfrecidas(List.of(messi)).build();

      Subasta subasta = Subasta.builder()
          .id("s-1").autor(sofia)
          .fechaInicio(LocalDateTime.now().minusHours(1))
          .fechaCierre(LocalDateTime.now().plusDays(1))
          .figuritaSubastada(messi)
          .ofertas(List.of(propuesta)).build();
      repositorioSubastas.guardar(subasta);

      // simular reserva previa
      coleccionLucas.getRepetidas().get(0).reservar(MetodoIntercambio.SUBASTA);
      repositorioColecciones.guardar(coleccionLucas, new CamposColeccion(true, false));

      service.cancelarOferta(lucas.getId(), "s-1", "o-1");

      Coleccion col = repositorioColecciones.buscarPorId("c-1", new CamposColeccion(true, false));
      assertEquals(0, col.getRepetidas().get(0).getCantidadReservada());
    }

    @Test
    void crearSubasta_reservaFiguritaSubastada() {
      service.crearSubasta("2", "ARG-10", 30, List.of(), 0);

      Coleccion col = repositorioColecciones.buscarPorId("c-2", new CamposColeccion(true, false));
      FiguritaIntercambiable repetida = col.getRepetidas().stream()
          .filter(r -> r.getFigurita().getId().equals("ARG-10"))
          .findFirst().orElseThrow();

      assertEquals(1, repetida.getCantidadReservada());
    }

    @Test
    void crearSubasta_duplicadaFiguritaActiva_lanzaExcepcion() {
      service.crearSubasta("2", "ARG-10", 30, List.of(), 0);

      BadRequestException ex = assertThrows(BadRequestException.class,
          () -> service.crearSubasta("2", "ARG-10", 30, List.of(), 0));

      assertTrue(ex.getMessage().contains("subasta activa para esta figurita"));
    }
  }
}