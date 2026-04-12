package app.repositories;

import app.model.entities.Propuesta;
import java.util.List;

public interface RepositorioPropuestas {
  Propuesta findById(String id);
  List<Propuesta> findAll();
  List<Propuesta> findByOrigenId(String userId);
  List<Propuesta> findByDestinoId(String userId);
  void save(Propuesta propuesta);
}