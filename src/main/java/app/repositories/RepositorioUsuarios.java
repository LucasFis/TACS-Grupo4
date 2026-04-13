package app.repositories;

import app.model.entities.Usuario;

public interface RepositorioUsuarios {

    Usuario findById(String id);

    void save(Usuario usuario);
}