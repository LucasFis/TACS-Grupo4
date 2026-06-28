package app.repositories;

import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.propuesta.PropuestasDto;
import app.model.entities.Propuesta;
import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioPropuestas {
    /**
     * Obtiene todas las propuestas sin filtros para fines estadísticos.
     *
     * @return lista completa de todas las propuestas del sistema
     */
    List<Propuesta> buscarTodosEstadisticas();
    List<Propuesta> buscarEstadisticasPorRango(LocalDateTime desde, LocalDateTime hasta);
    Propuesta buscarPorId(String id);
    int contar();
    void guardar(Propuesta propuesta);
    PaginaResultado<Propuesta> buscarTodos(String perfilId, PropuestasFiltro filtros);
}