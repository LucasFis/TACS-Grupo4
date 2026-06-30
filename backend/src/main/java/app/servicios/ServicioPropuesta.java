package app.servicios;

import app.dto.IntercambioDto;
import app.dto.PropuestaDto;
import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearPropuestaRequest;
import app.exceptions.BadRequestException;
import app.exceptions.ForbiddenException;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Propuesta;
import app.repositories.RepositorioCalificacion;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.repositories.impl.campos.CamposPerfil;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioPropuesta {

  private final RepositorioPropuestas repositorioPropuestas;
  private final RepositorioSubastas repositorioSubastas;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioColecciones repositorioColecciones;
  private final RepositorioCalificacion repositorioCalificacion;
  private final ServicioNotificacion notificacionService;
  private final MeterRegistry meterRegistry;

  /**
   * Crea una propuesta de intercambio. Valida que el usuario origen,
   * destino y figuritas existan. Verifica que la figurita buscada esté
   * en los faltantes del autor y reserva las figuritas ofrecidas.
   * El estado inicial es PENDIENTE. Notifica al destinatario.
   *
   * @param autorId identificador del perfil que crea la propuesta (autor)
   * @param request datos de la propuesta (destinatario, figurita buscada, figuritas ofrecidas)
   * @return datos de la propuesta creada como {@link PropuestaDto}
   * @throws app.exceptions.BadRequestException si la figurita buscada no está en faltantes del autor
   * @throws app.exceptions.NotFoundException  si no se encuentra el perfil, destino o alguna figurita
   */
  @Transactional
  public PropuestaDto crearPropuesta(String autorId, CrearPropuestaRequest request) {
    CamposPerfil sinCampos = new CamposPerfil(false);

    Perfil autor = repositorioPerfiles.buscarPorId(autorId, sinCampos);
    Perfil destino = repositorioPerfiles.buscarPorId(request.getDestinatarioId(), sinCampos);

    Figurita figuritaBuscada = repositorioFiguritas
        .buscarPorId(request.getFiguritaBuscadaId());

    if (!autor.getColeccion().tieneFaltante(figuritaBuscada)) {
      throw new BadRequestException("La figurita #" + figuritaBuscada.getNumero() + " no está en tus faltantes");
    }

    List<Figurita> figuritasOfrecidas = request.getFiguritasOfrecidasIds()
        .stream()
        .map(repositorioFiguritas::buscarPorId)
        .toList();


    autor.getColeccion().reservarRepetidas(figuritasOfrecidas, MetodoIntercambio.INTERCAMBIO);

    Propuesta propuesta = Propuesta.builder()
        .autor(autor)
        .destinatario(destino)
        .figuritaBuscada(figuritaBuscada)
        .figuritasOfrecidas(figuritasOfrecidas)
        .build();
    repositorioColecciones.guardar(autor.getColeccion());
    repositorioPropuestas.guardar(propuesta);
    meterRegistry.counter("propuestas_creadas_total", "metodo", "intercambio").increment();

    String cuerpo = "Tenes una nueva propuesta de: " + autor.getNombre()
        + " por la figurita " + figuritaBuscada.getJugador() + " de " + figuritaBuscada.getSeleccion();

    String link = "/intercambios/" + propuesta.getId();
    notificacionService.notificarInteresados(List.of(destino), cuerpo, link);

    return new PropuestaDto(propuesta);
  }

  /**
   * Obtiene una propuesta por su identificador, determinando si fue enviada o recibida
   * por el perfil que la solicita.
   *
   * @param propuestaId identificador de la propuesta a obtener
   * @param perfilId    identificador del perfil que solicita la propuesta
   * @return datos de la propuesta con la clasificación ENVIADA o RECIBIDA
   * @throws app.exceptions.NotFoundException si no se encuentra la propuesta
   */
   public PropuestaDto obtenerPorId(String propuestaId, String perfilId) {
      Propuesta propuesta = repositorioPropuestas.buscarPorId(propuestaId);
      if (propuesta == null) throw new NotFoundException("Propuesta no encontrada");

      String tipo = propuesta.getDestinatario().getId().equals(perfilId)
            ? "RECIBIDA"
            : "ENVIADA";
      return new PropuestaDto(propuesta, tipo);
  }

  /**
   * Acepta una propuesta de intercambio. Reserva la figurita buscada en la colección
   * del destinatario y ejecuta el intercambio de figuritas entre ambas partes.
   * Notifica al autor de la propuesta que fue aceptada.
   *
   * @param propuestaId identificador de la propuesta a aceptar
   * @param perfilId    identificador del perfil que acepta la propuesta (destinatario)
   * @throws app.exceptions.BadRequestException si la propuesta no está pendiente
   * @throws app.exceptions.NotFoundException  si no se encuentra la propuesta
   */
  @Transactional
  public void aceptar(String propuestaId, String perfilId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(propuestaId);

    Perfil autor = propuesta.getAutor();
    Perfil destinatario = propuesta.getDestinatario();

    Coleccion coleccionDestinatario = destinatario.getColeccion();

    coleccionDestinatario.reservarRepetidas(
        List.of(propuesta.getFiguritaBuscada()),
        MetodoIntercambio.INTERCAMBIO
    );

    propuesta.aceptar(perfilId);
    meterRegistry.counter("propuestas_transiciones_total", "estado", "aceptado", "origen", "intercambio").increment();
    registrarTiempoResolucion(propuesta, "aceptado");

    repositorioColecciones.guardar(autor.getColeccion());
    repositorioColecciones.guardar(coleccionDestinatario);
    repositorioPropuestas.guardar(propuesta);

    String link = "/intercambios/" + propuestaId;
    String cuerpo = "Tu propuesta de intercambio con " + propuesta.getDestinatario().getNombre() + " fue aceptada";
    notificacionService.notificarInteresados(List.of(propuesta.getAutor()), cuerpo, link);
  }

  /**
   * Rechaza una propuesta de intercambio. Libera las figuritas ofrecidas que estaban
   * reservadas y notifica al autor del rechazo.
   *
   * @param id       identificador de la propuesta a rechazar
   * @param perfilId identificador del perfil que rechaza la propuesta
   * @throws app.exceptions.BadRequestException si la propuesta no está pendiente
   * @throws app.exceptions.NotFoundException  si no se encuentra la propuesta
   */
  @Transactional
  public void rechazar(String id, String perfilId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(id);
    propuesta.rechazar(perfilId);
    meterRegistry.counter("propuestas_transiciones_total", "estado", "rechazado", "origen", "intercambio").increment();
    registrarTiempoResolucion(propuesta, "rechazado");

    Perfil autor = propuesta.getAutor();;

    repositorioColecciones.guardar(autor.getColeccion());

    repositorioPropuestas.guardar(propuesta);

    //Para Notificaciones
    String link = "/intercambios/" + id;
    String cuerpo = "Tu propuesta de intercambio con " + propuesta.getDestinatario().getNombre() + " fue rechazada";
    notificacionService.notificarInteresados(List.of(propuesta.getAutor()), cuerpo, link);
  }

  /**
   * Cancela una propuesta de intercambio. Libera las figuritas ofrecidas que estaban
   * reservadas en la colección del autor.
   *
   * @param id       identificador de la propuesta a cancelar
   * @param perfilId identificador del perfil que cancela la propuesta (autor)
   * @throws app.exceptions.BadRequestException si la propuesta no está pendiente
   * @throws app.exceptions.NotFoundException  si no se encuentra la propuesta
   */
  @Transactional
  public void cancelar(String id, String perfilId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(id);
    propuesta.cancelar(perfilId);
    meterRegistry.counter("propuestas_transiciones_total", "estado", "cancelado", "origen", "intercambio").increment();
    registrarTiempoResolucion(propuesta, "cancelado");

    Perfil autor = propuesta.getAutor();;

    repositorioColecciones.guardar(autor.getColeccion());

    this.repositorioPropuestas.guardar(propuesta);
  }

  /**
   * Verifica si las figuritas que el destinatario va a recibir en una propuesta
   * también están involucradas en otras transacciones pendientes del mismo perfil
   * (propuestas como autor/destinatario y subastas activas con oferta pendiente).
   *
   * @param propuestaId identificador de la propuesta a verificar
   * @param perfilId    identificador del perfil que va a aceptar (destinatario)
   * @return mapa con "tieneConflictos" (boolean) y "figuritasEnConflicto" (lista con id y jugador)
   */
  public Map<String, Object> verificarConflictos(String propuestaId, String perfilId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(propuestaId);
    if (!propuesta.getDestinatario().getId().equals(perfilId)) {
      throw new ForbiddenException("No sos el destinatario de esta propuesta");
    }
    List<Figurita> recibidas = propuesta.getFiguritasOfrecidas();
    if (recibidas == null) {
      recibidas = List.of();
    }

    List<Map<String, String>> figuritasEnConflicto = new ArrayList<>();

    for (Figurita figurita : recibidas) {
      int total = repositorioPropuestas.contarConflictos(figurita.getId(), perfilId, propuestaId)
          + repositorioSubastas.contarActivasConOfertaPendiente(figurita.getId(), perfilId);

      if (total > 0) {
        Map<String, String> entry = new HashMap<>();
        entry.put("id", figurita.getId());
        entry.put("jugador", figurita.getJugador());
        figuritasEnConflicto.add(entry);
      }
    }

    Map<String, Object> result = new HashMap<>();
    result.put("tieneConflictos", !figuritasEnConflicto.isEmpty());
    result.put("figuritasEnConflicto", figuritasEnConflicto);
    return result;
  }

  /**
   * Busca las propuestas de intercambio de un perfil de forma paginada, indicando
   * si cada intercambio ya fue calificado por el perfil.
   *
   * @param perfilId identificador del perfil del cual se buscarán las propuestas
   * @param filtros  criterios de filtrado (tipo: ENVIADAS/RECIBIDAS, paginación)
   * @return página de intercambios con indicación de calificación
   */
  public PaginaResultado<IntercambioDto> buscarPropuestas(String perfilId, PropuestasFiltro filtros) {
    PaginaResultado<Propuesta> resultado = this.repositorioPropuestas.buscarTodos(perfilId, filtros);

    return resultado.mapearA(p -> {
      boolean esEnviada = Objects.equals(filtros.tipo(), "ENVIADAS");

      String perfilCalificado = esEnviada
          ? p.getDestinatario().getId()
          : p.getAutor().getId();

      boolean yaCalificado = this.repositorioCalificacion.yaCalifico(
          perfilCalificado,
          perfilId,
          p.getId(),
          MetodoIntercambio.INTERCAMBIO
      );

      return new IntercambioDto(p, yaCalificado);
    });
  }

  /**
   * Registra cuánto tardó la propuesta desde su primer estado PENDIENTE hasta la transición
   * final. Insumo para decidir recordatorios por demora. Tag de conjunto cerrado.
   */
  private void registrarTiempoResolucion(Propuesta propuesta, String estadoFinal) {
    LocalDateTime inicio = propuesta.getEstado().get(0).getFecha();
    meterRegistry.timer("propuestas_tiempo_resolucion_segundos", "estado_final", estadoFinal)
        .record(Duration.between(inicio, LocalDateTime.now()));
  }
}