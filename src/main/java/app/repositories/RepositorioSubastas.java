package app.repositories;

import app.model.entities.Subasta;
import java.util.List;

public interface RepositorioSubastas {
    List<Subasta> findByUsuarioId(String userId);

    void save(Subasta subasta);
}