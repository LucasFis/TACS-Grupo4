package app.repositories.impl;

import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.model.entities.Usuario;
import app.repositories.RepositorioUsuarios;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioUsuariosEnMemoria implements RepositorioUsuarios {

    private final Map<String, Usuario> storage = new HashMap<>();

    @Override
    public Usuario findById(String id) {
        if(!storage.containsKey(id)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        return storage.get(id);
    }

    @Override
    public List<Usuario> buscarPorFiguritaFaltante(Figurita figurita) {
        return this.storage.values()
            .stream()
            .filter(u -> u.getColeccion().tieneFaltante(figurita))
            .toList();
    }

    @Override
    public List<Usuario> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public void save(Usuario usuario) {
        if(!storage.containsKey(usuario.getId())) {
            storage.put(usuario.getId(), usuario);
        }
    }
}