package app.repositories.impl;

import app.model.entities.Usuario;
import app.repositories.RepositorioUsuarios;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioUsuariosEnMemoria implements RepositorioUsuarios {

    private final Map<String, Usuario> storage = new HashMap<>();

    @Override
    public Usuario findById(String id) {
        return storage.get(id);
    }

    @Override
    public List<Usuario> findAll() {
        return storage.values().stream().toList();
    }

    @Override
    public void save(Usuario usuario) {
        if(!storage.containsKey(usuario.getId())) {
            storage.put(usuario.getId(), usuario);
        }
    }
}