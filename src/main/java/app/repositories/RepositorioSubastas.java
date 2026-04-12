package app.repositories;

import app.model.entities.Subasta;
import java.util.List;

public interface RepositorioSubastas {
  Subasta findById(String id);
  List<Subasta> findAll();
  List<Subasta> findByUsuarioId(String userId);
  void save(Subasta subasta);
}