package app.repositories;

import app.model.entities.Usuario;

public interface RepositorioUsuarios {
  void guardar(Usuario usuario);
  Usuario buscarPorNombre(String nombre);

  boolean existePorNombre(String nombre);
}
