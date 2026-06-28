package app.repositories;

import app.dto.paginacion.PaginaResultado;
import app.model.entities.Calificacion;
import app.model.entities.MetodoIntercambio;

public interface RepositorioCalificacion {
  void guardar(Calificacion calificacion);
  PaginaResultado<Calificacion> buscarPorDestinatario(String perfilId, Integer pagina, Integer limite);
  /**
   * Verifica si un perfil ya calificó a otro en una transacción específica.
   *
   * @param perfilDestinoId identificador del perfil que recibiría la calificación
   * @param perfilAutorId   identificador del perfil que emitió la calificación
   * @param transaccionId   identificador de la transacción (intercambio o subasta)
   * @param tipoTransaccion tipo de transacción (INTERCAMBIO o SUBASTA)
   * @return {@code true} si el autor ya calificó al destino en esa transacción
   */
  boolean yaCalifico(String perfilDestinoId, String perfilAutorId, String transaccionId, MetodoIntercambio tipoTransaccion);
}
