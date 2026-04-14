package app.dto;

import app.model.notificador.Mensaje;
import app.model.notificador.Notificacion;
import lombok.Getter;

@Getter
public class NotificacionesDto {
  String id;
  Mensaje mensaje;
  UsuarioDto usuario;

  public NotificacionesDto(Notificacion notificacion) {
    this.id = notificacion.getId();
    this.mensaje = notificacion.getMensaje();
    this.usuario = new UsuarioDto(notificacion.getUsuario());
  }
}
