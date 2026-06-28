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

  /**
   * Notifica a una lista de perfiles interesados con un mensaje de texto,
   * sin incluir un enlace adicional. Es un caso particular de
   * {@link #notificarInteresados(List, String, String)} con link {@code null}.
   *
   * @param interesados lista de perfiles que recibirán la notificación
   * @param cuerpo      contenido del mensaje de la notificación
   */
  public void notificarInteresados(List<Perfil> interesados, String cuerpo) {
      notificarInteresados(interesados, cuerpo, null);
  }

  /**
   * Notifica a una lista de perfiles interesados con un mensaje de texto
   * y un enlace opcional. Por cada perfil se crea y persiste una nueva
   * {@link app.model.notificador.Notificacion}.
   *
   * @param interesados lista de perfiles que recibirán la notificación
   * @param cuerpo      contenido del mensaje de la notificación
   * @param link        enlace opcional asociado a la notificación (puede ser {@code null})
   */
  public void notificarInteresados(List<Perfil> interesados, String cuerpo, String link) {
      interesados.forEach(perfil -> {
          Mensaje mensaje = new Mensaje(cuerpo, LocalDateTime.now());
          repositorioNotificaciones.guardar(new Notificacion(mensaje, perfil, link));
      });
  }

  /**
   * Obtiene todas las notificaciones de un perfil ordenadas por fecha de
   * creación descendente.
   *
   * @param perfilId identificador del perfil del cual se obtendrán las notificaciones
   * @return lista de notificaciones del perfil, ordenadas de la más reciente a la más antigua
   */
  public List<Notificacion> obtenerPorPerfil(String perfilId) {
      return repositorioNotificaciones.buscarPorPerfilFechaDesc(perfilId);
  }

  /**
   * Marca todas las notificaciones de un perfil como leídas.
   *
   * @param perfilId identificador del perfil cuyas notificaciones se marcarán como leídas
   */
  public void marcarTodasLeidas(String perfilId) {
      List<Notificacion> notis = repositorioNotificaciones.buscarPorPerfilFechaDesc(perfilId);
      notis.forEach(Notificacion::marcarLeida);
      this.repositorioNotificaciones.guardar(notis);
  }
}
