package app.repositories.impl;

import app.model.entities.Subasta;
import app.repositories.RepositorioSubastas;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioSubastasEnMemoria implements RepositorioSubastas {

    private final Map<String, Subasta> storage = new HashMap<>();

    @Override
    public List<Subasta> findByUsuarioId(String userId) {
        return storage.values().stream()
                .filter(s -> s.getUsuario().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public void save(Subasta subasta) {
        storage.put(subasta.getId(), subasta);
    }
}