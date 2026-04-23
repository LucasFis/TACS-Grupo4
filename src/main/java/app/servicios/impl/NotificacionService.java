package app.servicios.impl;

import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Usuario;
import app.model.notificador.Mensaje;
import app.model.notificador.Notificacion;
import app.repositories.RepositorioNotificaciones;
import app.servicios.INotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@Service
public class NotificacionService implements INotificacionService {

  private final RepositorioNotificaciones repositorioNotificaciones;

  public void notificarInteresados(List<Usuario> interesados, String cuerpo) {
    interesados.forEach(u -> {
      Mensaje mensaje = new Mensaje(cuerpo, LocalDateTime.now());
      this.repositorioNotificaciones.guardar(new Notificacion(mensaje, u));
    });
  }
}
