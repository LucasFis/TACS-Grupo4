package app.repositories;

import app.model.entities.Propuesta;
import java.util.List;

public interface RepositorioPropuestas {
    // Propuestas enviadas
    List<Propuesta> buscarPorAutorId(String userId);
    // Propuestas recibidas
    List<Propuesta> buscarPorDestinatarioId(String userId);
    List<Propuesta> buscarTodos();
    Propuesta buscarPorId(String id);
    int contar();
    void guardar(Propuesta propuesta);
}