package app.repositories;

import app.model.entities.Propuesta;
import java.util.List;

public interface RepositorioPropuestas {
    // Propuestas enviadas
    List<Propuesta> findByOrigenId(String userId);

    // Propuestas recibidas
    List<Propuesta> findByDestinoId(String userId);

    void save(Propuesta propuesta);
}