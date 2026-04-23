package app.repositories;

import app.model.entities.Subasta;
import java.util.List;

public interface RepositorioSubastas {
    List<Subasta> buscarPorUsuarioId(String userId);

    List<Subasta> buscarTodos();

    Subasta buscarPorId(String id);

    int contar();

    void guardar(Subasta subasta);
}