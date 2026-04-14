package app.repositories;

import app.model.entities.Subasta;
import java.util.List;

public interface RepositorioSubastas {
    List<Subasta> findByUsuarioId(String userId);

    List<Subasta> findAll();

    int count();

    void save(Subasta subasta);
}