package app.repositories.impl;

import app.model.entities.Propuesta;
import app.repositories.RepositorioPropuestas;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPropuestasEnMemoria implements RepositorioPropuestas {

    private final Map<String, Propuesta> storage = new HashMap<>();

    @Override
    public List<Propuesta> findByOrigenId(String userId) {
        return storage.values().stream()
                .filter(p -> p.getUsuarioOrigen().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Propuesta> findByDestinoId(String userId) {
        return storage.values().stream()
                .filter(p -> p.getUsuarioDestino().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public void save(Propuesta propuesta) {
        storage.put(propuesta.getId(), propuesta);
    }
}