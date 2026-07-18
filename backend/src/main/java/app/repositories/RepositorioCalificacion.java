package app.repositories;

import app.dto.paginacion.PaginaResultado;
import app.model.entities.Calificacion;
import app.model.entities.MetodoIntercambio;
import java.util.Set;

public interface RepositorioCalificacion {
  void guardar(Calificacion calificacion);
  PaginaResultado<Calificacion> buscarPorDestinatario(String perfilId, Integer pagina, Integer limite);
  boolean yaCalifico(String perfilDestinoId, String perfilAutorId, String transaccionId, MetodoIntercambio tipoTransaccion);

  /**
   * Devuelve el subconjunto de {@code transaccionIds} donde {@code autorId} ya emitió
   * una calificación del tipo indicado. Reemplaza N llamadas a {@link #yaCalifico} en listas
   * paginadas por una sola query con {@code $in}.
   */
  Set<String> obtenerTransaccionesCalificadas(String autorId, MetodoIntercambio tipo, Set<String> transaccionIds);
}
