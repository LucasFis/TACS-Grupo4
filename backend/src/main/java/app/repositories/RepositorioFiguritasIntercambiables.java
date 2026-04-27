package app.repositories;

import app.model.entities.FiguritaIntercambiable;
import java.util.List;

public interface RepositorioFiguritasIntercambiables {
  List<FiguritaIntercambiable> buscarPorFiguritaIds(List<String> figuritaIds);
  List<FiguritaIntercambiable> buscarPorUsuarioId(String usuarioId);
  void guardar(FiguritaIntercambiable figuritaIntercambiable);
}