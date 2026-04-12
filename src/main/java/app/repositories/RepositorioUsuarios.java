package app.repositories;

import app.model.entities.Usuario;
import java.util.List;

public interface RepositorioUsuarios {
  Usuario findById(String id);
  List<Usuario> findAll();
  void save(Usuario usuario);
}