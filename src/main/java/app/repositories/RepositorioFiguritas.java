package app.repositories;

import app.model.entities.Figurita;

public interface RepositorioFiguritas {
  public Figurita findById(String id);
  public void save(Figurita figurita);
}
