package app.servicios.impl;

import app.client.ImagenJugadorProveedor;
import app.model.entities.Figurita;
import app.repositories.RepositorioFiguritas;
import app.servicios.ServicioEnriquecimiento;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ServicioDeAgregacionDeDatos implements ServicioEnriquecimiento {

  private static final Logger log = LoggerFactory.getLogger(ServicioDeAgregacionDeDatos.class);
  private static final String STATUS_COMPLETADO = "COMPLETADO";
  private static final Duration TIMEOUT_PROCESAMIENTO = Duration.ofMinutes(10);
  private static final Duration TTL_REFRESCO_IMAGEN = Duration.ofDays(1);
  private static final int TAMANIO_PAGINA = 30;

  private final ImagenJugadorProveedor imagenProveedor;
  private final RepositorioFiguritas repositorioFiguritas;

  public ServicioDeAgregacionDeDatos(ImagenJugadorProveedor imagenProveedor,
                                     RepositorioFiguritas repositorioFiguritas) {
    this.imagenProveedor = imagenProveedor;
    this.repositorioFiguritas = repositorioFiguritas;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Ejecuta de forma asíncrona el enriquecimiento de todas las figuritas pendientes.
   * Es idempotente: si una figurita ya fue procesada por otra instancia, la ignora.
   * Los errores individuales se registran pero no interrumpen el procesamiento del resto.
   * </p>
   */
  @Override
  @Async
  public void enriquecer() {
    Set<String> intentados = new HashSet<>();
    List<Figurita> pendientes;
    do {
      pendientes = repositorioFiguritas
          .buscarPendientes(TIMEOUT_PROCESAMIENTO, TTL_REFRESCO_IMAGEN, TAMANIO_PAGINA)
          .stream()
          .filter(figurita -> intentados.add(figurita.getId()))
          .toList();

      for (Figurita figurita : pendientes) {
        try {
          procesarFigurita(figurita);
        } catch (Exception e) {
          log.error("Error inesperado al enriquecer '{}': {}", figurita.getJugador(), e.getMessage());
        }
      }
    } while (!pendientes.isEmpty());
  }

  /**
   * Procesa una figurita individual: la reclama para procesamiento (evitando
   * concurrencia), busca su imagen a través del proveedor externo y persiste
   * el resultado.
   *
   * @param figurita la figurita a enriquecer con la imagen del jugador
   */
  private void procesarFigurita(Figurita figurita) {
    Figurita reclamada = repositorioFiguritas
        .reclamarParaProcesamiento(figurita.getId(), TIMEOUT_PROCESAMIENTO, TTL_REFRESCO_IMAGEN);
    if (reclamada == null) {
      // otra instancia se adelantó
      return;
    }

    String url = imagenProveedor.buscarImagen(figurita.getJugador()).orElse(null);
    figurita.setImagenUrl(url);
    figurita.setImagenStatus(STATUS_COMPLETADO);
    figurita.setImagenCreadoEn(LocalDateTime.now());
    repositorioFiguritas.guardar(figurita);

    if (url != null) {
      log.info("Imagen obtenida: {}", figurita.getJugador());
    }
  }
}
