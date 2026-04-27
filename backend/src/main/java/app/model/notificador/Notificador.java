package app.model.notificador;

import app.model.entities.Perfil;


public class Notificador {
  public AdapterNotificacion metodo;

  Notificador(AdapterNotificacion metodo) {
    this.metodo = metodo;
  }

  public void enviarNotificacion(Mensaje mensaje, Perfil usuario) {
    this.metodo.notificar(mensaje, usuario);
  }
}
