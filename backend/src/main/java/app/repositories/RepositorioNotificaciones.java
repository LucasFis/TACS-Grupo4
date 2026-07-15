package app.repositories;

import app.dto.filtros.NotificacionesFiltro;
import app.dto.paginacion.PaginaResultado;
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

    /**
     * Busca notificaciones de un perfil con paginación y filtro por estado de lectura.
     *
     * @param perfilId identificador del perfil
     * @param filtro   filtros y parámetros de paginación
     * @return página de resultados con notificaciones ordenadas por fecha descendente
     */
    PaginaResultado<Notificacion> buscarPorPerfilPaginado(String perfilId, NotificacionesFiltro filtro);

    void guardar(Notificacion notificacion);
    void guardar(List<Notificacion> notificaciones);

    List<Notificacion> buscarPorPerfil(Perfil perfil);

    long contarNoLeidas(String perfilId);
}