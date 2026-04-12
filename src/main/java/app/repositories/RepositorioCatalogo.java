package app.repositories;

import app.model.entities.Figurita;
import java.util.List;

public interface RepositorioCatalogo {
  List<Figurita> findAll();
  Figurita findById(String id);
  void save(Figurita figurita);
}