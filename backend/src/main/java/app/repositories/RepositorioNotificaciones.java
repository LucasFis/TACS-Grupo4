package app.repositories;

import app.model.entities.Perfil;
import app.model.notificador.Notificacion;

import java.util.List;

public interface RepositorioNotificaciones {
  void guardar(Notificacion notificacion);
  List<Notificacion> buscarPorUsuario(Perfil usuario);
}
