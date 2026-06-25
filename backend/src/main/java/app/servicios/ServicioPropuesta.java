package app.servicios;

import app.dto.IntercambioDto;
import app.dto.PropuestaDto;
import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearPropuestaRequest;
import app.exceptions.BadRequestException;
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
import app.exceptions.NotFoundException;

import java.util.List;

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
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioColecciones repositorioColecciones;
  private final RepositorioCalificacion repositorioCalificacion;
  private final ServicioNotificacion notificacionService;
  private final MeterRegistry meterRegistry;

  /**
   * Crea una propuesta de intercambio. Valida que el usuario origen,
   * destino y figuritas existan. El estado inicial es PENDIENTE.
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
        + " por la figurita numero: " + figuritaBuscada.getNumero();

    notificacionService.notificarInteresados(List.of(destino), cuerpo);

    return new PropuestaDto(propuesta);
  }

   public PropuestaDto obtenerPorId(String propuestaId, String perfilId) {
      Propuesta propuesta = repositorioPropuestas.buscarPorId(propuestaId);
      if (propuesta == null) throw new NotFoundException("Propuesta no encontrada");

      String tipo = propuesta.getDestinatario().getId().equals(perfilId)
            ? "RECIBIDA"
            : "ENVIADA";
      return new PropuestaDto(propuesta, tipo);
  }

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
    String cuerpo = "Tu propuesta de intercambio fue aceptada";
    notificacionService.notificarInteresados(List.of(propuesta.getAutor()), cuerpo, link);
  }

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
    String cuerpo = "Tu propuesta de intercambio fue rechazada";
    notificacionService.notificarInteresados(List.of(propuesta.getAutor()), cuerpo, link);
  }

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