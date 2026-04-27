package app.repositories.impl;

import app.model.entities.Perfil;
import app.model.notificador.Notificacion;
import app.repositories.RepositorioNotificaciones;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RepositorioNotificacionesEnMemoria implements RepositorioNotificaciones {

  private final Map<String, Notificacion> storage = new HashMap<>();

  @Override
  public void guardar(Notificacion notificacion) {
    if (storage.containsKey(notificacion.getId())) {
      storage.put(notificacion.getId(), notificacion);
      return;
    }

    int nextId = storage.keySet().stream()
        .mapToInt(Integer::parseInt)
        .max()
        .orElse(0) + 1;

    String nuevoId = String.valueOf(nextId);
    notificacion.setId(nuevoId);

    storage.put(nuevoId, notificacion);
  }

  @Override
  public List<Notificacion> buscarPorUsuario(Perfil usuario) {
    return storage.values().stream()
        .filter(n -> n.getUsuario().getId().equals(usuario.getId()))
        .toList();
  }
}
