package app.repositories.impl;

import app.MongoTestBase;
import app.dto.filtros.NotificacionesFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Usuario;
import app.model.notificador.Mensaje;
import app.model.notificador.Notificacion;
import java.util.List;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepositorioNotificacionesTest extends MongoTestBase {

  private List<MedioDeContacto> telegram(String numero) {
    return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
  }

  @Test
  void buscarNotificacionesDeUnUsuario() {
    LocalDateTime fecha = LocalDateTime.now();
    Mensaje mensaje = new Mensaje("Mensaje1", fecha);

    Usuario user = new Usuario("u-1", Rol.USUARIO,"lucas", "fiscella");
    Perfil perfil1 = Perfil.builder()
        .id("1").usuario(user).nombre("Juan")
        .mediosDeContacto(telegram("@juan"))
        .build();

    user = new Usuario("u-2", Rol.USUARIO,"lucas", "fiscella");
    Perfil perfil2 = Perfil.builder()
        .id("2").usuario(user)
        .nombre("Miguel").mediosDeContacto(telegram("@miguel"))
        .build();

    Notificacion notificacion1 = new Notificacion(mensaje, perfil1);
    Notificacion notificacion2 = new Notificacion(mensaje, perfil1);
    Notificacion notificacion3 = new Notificacion(mensaje, perfil1);
    Notificacion notificacion4 = new Notificacion(mensaje, perfil2);
    Notificacion notificacion5 = new Notificacion(mensaje, perfil2);

    repositorioNotificaciones.guardar(notificacion1);
    repositorioNotificaciones.guardar(notificacion2);
    repositorioNotificaciones.guardar(notificacion3);
    repositorioNotificaciones.guardar(notificacion4);
    repositorioNotificaciones.guardar(notificacion5);

    assertEquals(3, repositorioNotificaciones.buscarPorPerfilFechaDesc(perfil1.getId()).size());
  }

  @Test
  void buscarNotificacionesPaginadas() {
    LocalDateTime fecha = LocalDateTime.now();
    Mensaje mensaje = new Mensaje("Mensaje1", fecha);

    Usuario user = new Usuario("u-1", Rol.USUARIO, "lucas", "fiscella");
    Perfil perfil1 = Perfil.builder()
        .id("1").usuario(user).nombre("Juan")
        .mediosDeContacto(telegram("@juan"))
        .build();

    Notificacion n1 = new Notificacion(mensaje, perfil1);
    Notificacion n2 = new Notificacion(mensaje, perfil1);
    Notificacion n3 = new Notificacion(mensaje, perfil1);

    repositorioNotificaciones.guardar(n1);
    repositorioNotificaciones.guardar(n2);
    repositorioNotificaciones.guardar(n3);

    NotificacionesFiltro filtroPagina1 = new NotificacionesFiltro(null, 1, 2);
    PaginaResultado<Notificacion> resultado = repositorioNotificaciones.buscarPorPerfilPaginado("1", filtroPagina1);

    assertEquals(2, resultado.contenido().size());
    assertEquals(3, resultado.cantidadDeElementos());
    assertEquals(2, resultado.cantidadDePaginas());
    assertEquals(1, resultado.numero());
  }

  @Test
  void buscarNotificacionesFiltradasPorLeidas() {
    LocalDateTime fecha = LocalDateTime.now();
    Mensaje mensaje = new Mensaje("Mensaje1", fecha);

    Usuario user = new Usuario("u-1", Rol.USUARIO, "lucas", "fiscella");
    Perfil perfil1 = Perfil.builder()
        .id("1").usuario(user).nombre("Juan")
        .mediosDeContacto(telegram("@juan"))
        .build();

    Notificacion leida = new Notificacion(mensaje, perfil1);
    leida.marcarLeida();
    Notificacion noLeida = new Notificacion(mensaje, perfil1);

    repositorioNotificaciones.guardar(leida);
    repositorioNotificaciones.guardar(noLeida);

    NotificacionesFiltro filtroLeidas = new NotificacionesFiltro(true, 1, 10);
    PaginaResultado<Notificacion> resultadoLeidas = repositorioNotificaciones.buscarPorPerfilPaginado("1", filtroLeidas);
    assertEquals(1, resultadoLeidas.contenido().size());
    assertEquals(1, resultadoLeidas.cantidadDeElementos());

    NotificacionesFiltro filtroNoLeidas = new NotificacionesFiltro(false, 1, 10);
    PaginaResultado<Notificacion> resultadoNoLeidas = repositorioNotificaciones.buscarPorPerfilPaginado("1", filtroNoLeidas);
    assertEquals(1, resultadoNoLeidas.contenido().size());
    assertEquals(1, resultadoNoLeidas.cantidadDeElementos());

    NotificacionesFiltro filtroTodas = new NotificacionesFiltro(null, 1, 10);
    PaginaResultado<Notificacion> resultadoTodas = repositorioNotificaciones.buscarPorPerfilPaginado("1", filtroTodas);
    assertEquals(2, resultadoTodas.contenido().size());
    assertEquals(2, resultadoTodas.cantidadDeElementos());
  }
}