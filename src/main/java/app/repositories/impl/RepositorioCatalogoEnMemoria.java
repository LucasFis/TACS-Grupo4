package app.repositories.impl;

import app.model.entities.Figurita;
import app.repositories.RepositorioCatalogo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioCatalogoEnMemoria implements RepositorioCatalogo {

  private final Map<String, Figurita> storage = new HashMap<>();

  @Override
  public List<Figurita> findAll() {
    return new ArrayList<>(storage.values());
  }

  @Override
  public Figurita findById(String id) {
    return storage.get(id);
  }

  @Override
  public void save(Figurita figurita) {
    storage.put(figurita.getId(), figurita);
  }
}