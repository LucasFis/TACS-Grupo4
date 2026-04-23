package app.repositories.impl;

import app.model.entities.Coleccion;
import app.model.entities.Usuario;
import app.model.notificador.Mensaje;
import app.model.notificador.Notificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepositorioNotificacionesEnMemoriaTest {

  private RepositorioNotificacionesEnMemoria repositorio;


  @BeforeEach
  void setUp() {
    repositorio = new RepositorioNotificacionesEnMemoria();
  }

  @Test
  void buscarNotificacionesDeUnUsuario() {
    LocalDateTime fecha = LocalDateTime.now();
    Mensaje mensaje = new Mensaje("Mensaje1", fecha);
    Usuario user1 = new Usuario(
        "1", "Juan", new Coleccion(), "12223123123", new ArrayList<>());
    Usuario user2 = new Usuario(
        "2", "Miguel", new Coleccion(), "9998262", new ArrayList<>());

    Notificacion notificacion1 = new Notificacion(mensaje, user1);
    Notificacion notificacion2 = new Notificacion(mensaje, user1);
    Notificacion notificacion3 = new Notificacion(mensaje, user1);
    Notificacion notificacion4 = new Notificacion(mensaje, user2);
    Notificacion notificacion5 = new Notificacion(mensaje, user2);

    this.repositorio.guardar(notificacion1);
    this.repositorio.guardar(notificacion2);
    this.repositorio.guardar(notificacion3);
    this.repositorio.guardar(notificacion4);
    this.repositorio.guardar(notificacion5);

    assertEquals(3, repositorio.buscarPorUsuario(user1).size());
  }
}
