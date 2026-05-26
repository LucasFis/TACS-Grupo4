package app.servicios;

import app.model.entities.Perfil;
import app.model.notificador.Mensaje;
import app.model.notificador.Notificacion;
import app.repositories.RepositorioNotificaciones;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ServicioNotificacion {

  private final RepositorioNotificaciones repositorioNotificaciones;

  //Generico para notificaciones sin link.
  public void notificarInteresados(List<Perfil> interesados, String cuerpo) {
      notificarInteresados(interesados, cuerpo, null);
  }

  public void notificarInteresados(List<Perfil> interesados, String cuerpo, String link) {
      interesados.forEach(perfil -> {
          Mensaje mensaje = new Mensaje(cuerpo, LocalDateTime.now());
          repositorioNotificaciones.guardar(new Notificacion(mensaje, perfil, link));
      });
  }

  public List<Notificacion> obtenerPorPerfil(String perfilId) {
      return repositorioNotificaciones.buscarPorPerfilFechaDesc(perfilId);
  }

  public void marcarTodasLeidas(String perfilId) {
      List<Notificacion> notis = repositorioNotificaciones.buscarPorPerfilFechaDesc(perfilId);
      this.repositorioNotificaciones.guardar(notis);
  }
}
