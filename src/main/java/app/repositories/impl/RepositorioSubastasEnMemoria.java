package app.repositories.impl;

import app.model.entities.Subasta;
import app.repositories.RepositorioSubastas;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioSubastasEnMemoria implements RepositorioSubastas {

    private final Map<String, Subasta> storage = new HashMap<>();

    @Override
    public List<Subasta> buscarPorUsuarioId(String userId) {
        return storage.values().stream()
                .filter(s -> s.getUsuario().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Subasta> buscarTodos() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int contar() {
        return storage.size();
    }
  
    @Override
    public Subasta buscarPorId(String id) {
        Subasta subasta = storage.get(id);
        if (subasta == null) {
            throw new RuntimeException("Subasta no encontrada");
        }
        return subasta;
    }

    @Override
    public void guardar(Subasta subasta) {
        storage.put(subasta.getId(), subasta);
    }
}