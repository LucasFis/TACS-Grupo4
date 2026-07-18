package app.repositories;

import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.EstadoProceso;
import app.model.entities.Propuesta;
import java.time.LocalDateTime;
import java.util.Map;

public interface RepositorioPropuestas {
    Map<EstadoProceso, Long> contarPorEstadoEnRango(LocalDateTime desde, LocalDateTime hasta);
    Propuesta buscarPorId(String id);
    int contar();
    void guardar(Propuesta propuesta);
    PaginaResultado<Propuesta> buscarTodos(String perfilId, PropuestasFiltro filtros);
    int contarConflictos(String figuritaId, String perfilId, String excluirPropuestaId);
}