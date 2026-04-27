package app.model.notificador;

import app.model.entities.Perfil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notificacion {
  String id;
  Mensaje mensaje;
  Perfil usuario;

  public Notificacion(Mensaje mensaje, Perfil usuario) {
    this.mensaje = mensaje;
    this.usuario = usuario;
  }
}
