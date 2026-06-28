package app.repositories;

import app.model.entities.Perfil;
import app.model.notificador.Notificacion;

import java.util.List;

public interface RepositorioNotificaciones {
    /**
     * Busca todas las notificaciones de un perfil ordenadas por fecha de creación descendente.
     *
     * @param perfilId identificador del perfil
     * @return lista de notificaciones, de la más reciente a la más antigua
     */
    List<Notificacion> buscarPorPerfilFechaDesc(String perfilId);

    void guardar(Notificacion notificacion);
    void guardar(List<Notificacion> notificaciones);

    List<Notificacion> buscarPorPerfil(Perfil perfil);
}