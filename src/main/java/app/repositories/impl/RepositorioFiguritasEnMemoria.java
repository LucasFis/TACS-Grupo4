package app.repositories.impl;

import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.repositories.RepositorioFiguritas;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;

@Repository
public class RepositorioFiguritasEnMemoria implements RepositorioFiguritas {
  private final Map<String, Figurita> storage = new HashMap<>();

  @Override
  public Figurita findById(String id) {
      Figurita figurita = storage.values()
        .stream()
        .filter(c -> c.getId().equals(id))
        .findFirst().orElse(null);

    if(figurita == null) {
      throw new NotFoundException("No se encontro la figurita");
    }

    return figurita;
  }

  public void save(Figurita figurita) {
    this.storage.put(figurita.getId(), figurita);

  }
}
