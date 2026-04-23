package app.repositories.impl;

import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.Propuesta;
import app.repositories.RepositorioPropuestas;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPropuestasEnMemoria implements RepositorioPropuestas {

    private final Map<String, Propuesta> storage = new HashMap<>();

    @Override
    public List<Propuesta> buscarPorOrigenId(String userId) {
        return storage.values().stream()
                .filter(p -> p.getUsuarioOrigen().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Propuesta> buscarPorDestinoId(String userId) {
        return storage.values().stream()
                .filter(p -> p.getUsuarioDestino().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Propuesta> buscarTodos() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void guardar(Propuesta propuesta) {

      this.storage.putIfAbsent(propuesta.getId(), propuesta);
    }

    @Override
    public Propuesta buscarPorId(String id){
        Propuesta propuesta = storage.get(id);

        if (propuesta == null) {
            throw new RuntimeException("Propuesta no encontrada");
        }
        return propuesta;
    }

    @Override
    public int contar() {
        return storage.size();
    }

}