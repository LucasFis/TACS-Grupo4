package app.model.notificador;

import app.model.entities.Perfil;

public interface AdapterNotificacion {
  public void notificar(Mensaje mensaje, Perfil receptor);
}
