package app.servicios;

import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Usuario;
import app.model.notificador.Mensaje;
import app.model.notificador.Notificacion;
import app.repositories.RepositorioNotificaciones;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

  private final RepositorioNotificaciones repositorioNotificaciones;

  public NotificacionService(RepositorioNotificaciones repositorioNotificaciones) {
    this.repositorioNotificaciones = repositorioNotificaciones;
  }

  public void notificarInteresados(List<Usuario> interesados, String cuerpo) {
    interesados.forEach(u -> {
      Mensaje mensaje = new Mensaje(cuerpo, LocalDateTime.now());
      this.repositorioNotificaciones.save(new Notificacion(mensaje, u));
    });
  }
}
