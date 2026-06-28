package app.servicios.impl;

import app.MongoTestBase;
import app.dto.request.EditarRepetidaRequest;
import app.exceptions.BadRequestException;
import app.model.entities.*;
import java.util.List;

import app.repositories.impl.campos.CamposColeccion;
import app.repositories.impl.campos.CamposPerfil;
import app.servicios.ServicioColeccion;
import app.servicios.ServicioNotificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ServicioColeccionTest extends MongoTestBase {

  @Autowired
  private ServicioNotificacion notificacionService;

  private ServicioColeccion service;

  private Figurita messi;
  private Coleccion coleccion;

  private String perfilId;

  @BeforeEach
  void setUp() {
    service = new ServicioColeccion(repositorioFiguritas, repositorioColecciones,
        repositorioPerfiles, notificacionService);

    messi = Figurita.builder()
        .id("ARG-10")
        .numero(10)
        .jugador("Messi")
        .seleccion(Seleccion.ARGENTINA)
        .build();

    repositorioFiguritas.guardar(messi);

    coleccion = new Coleccion();
    repositorioColecciones.guardar(coleccion);

    perfilId = "10";
  }

  @Test
  void agregarFaltante_agregaFiguritaAColeccion() {
    service.agregarFaltante(coleccion.getId(), "ARG-10");

    assertEquals(1, repositorioColecciones.buscarPorId(coleccion.getId(), new CamposColeccion(false, true)).getFaltantes().size());
    assertEquals(messi.getId(), repositorioColecciones.buscarPorId(coleccion.getId(), new CamposColeccion(false, true)).getFaltantes().get(0).getId());
  }

  @Test
  void agregarFaltante_figuritaDuplicada_lanzaExcepcion() {
    coleccion.agregarFaltante(messi);
    repositorioColecciones.guardar(coleccion);

    assertThrows(BadRequestException.class,
        () -> service.agregarFaltante(coleccion.getId(), "ARG-10"));
  }

  @Test
  void agregarRepetida_agregaFiguritaYNotificaInteresados() {
    Usuario user = new Usuario("u-2", Rol.USUARIO, "lucas", "fiscella");
    repositorioUsuarios.guardar(user);
    Coleccion coleccion2 = new Coleccion();
    coleccion2.agregarFaltante(messi);
    repositorioColecciones.guardar(coleccion2);
    Perfil interesado = Perfil.builder()
        .id("2").usuario(user).nombre("Sofía")
        .coleccion(coleccion2)
        .build();
    repositorioPerfiles.guardar(interesado);

    service.agregarRepetida(this.coleccion.getId(), perfilId, "ARG-10", 2, List.of(MetodoIntercambio.INTERCAMBIO));

    Coleccion coleccion = repositorioColecciones.buscarPorId(this.coleccion.getId(), new CamposColeccion(true, false));

    assertEquals(1, coleccion.getRepetidas().size());
    assertEquals(1, repositorioNotificaciones.buscarPorPerfil(interesado).size());
  }

  @Test
  void agregarRepetida_sinInteresados_noNotifica() {
    service.agregarRepetida(this.coleccion.getId(), perfilId,"ARG-10", 2, List.of(MetodoIntercambio.INTERCAMBIO));

    repositorioPerfiles.buscarTodos(new CamposPerfil(false)).forEach(p -> assertEquals(0, repositorioNotificaciones.buscarPorPerfil(p).size()));
  }

  @Test
  void editarRepetida_actualizaCantidadCorrectamente() {
    FiguritaIntercambiable repetida = FiguritaIntercambiable.builder()
        .figurita(messi)
        .cantidadExistente(5)
        .cantidadReservada(2)
        .metodos(List.of(MetodoIntercambio.INTERCAMBIO))
        .build();

    coleccion.setRepetidas(List.of(repetida));
    repositorioColecciones.guardar(coleccion);

    EditarRepetidaRequest request = new EditarRepetidaRequest(
        7,
        List.of(MetodoIntercambio.INTERCAMBIO)
    );

    service.editarRepetida(coleccion.getId(), messi.getId(), request);

    FiguritaIntercambiable actualizada =
        repositorioColecciones.buscarRepetida(coleccion.getId(), messi.getId());

    assertEquals(7, actualizada.getCantidadExistente());
  }

  @Test
  void editarRepetida_lanzaErrorSiCantidadMenorAReservada() {
    FiguritaIntercambiable repetida = FiguritaIntercambiable.builder()
        .figurita(messi)
        .cantidadExistente(5)
        .cantidadReservada(3)
        .metodos(List.of(MetodoIntercambio.INTERCAMBIO))
        .build();

    coleccion.setRepetidas(List.of(repetida));
    repositorioColecciones.guardar(coleccion);

    EditarRepetidaRequest request = new EditarRepetidaRequest(
        2,
        List.of(MetodoIntercambio.INTERCAMBIO)
    );

    assertThrows(
        BadRequestException.class,
        () -> service.editarRepetida(
            coleccion.getId(),
            messi.getId(),
            request
        )
    );
  }

  @Test
  void editarRepetida_permitaAgregarMetodoNuevo() {
    FiguritaIntercambiable repetida = FiguritaIntercambiable.builder()
        .figurita(messi)
        .cantidadExistente(5)
        .cantidadReservada(0)
        .metodos(List.of(MetodoIntercambio.INTERCAMBIO))
        .build();

    coleccion.setRepetidas(List.of(repetida));
    repositorioColecciones.guardar(coleccion);

    EditarRepetidaRequest request = new EditarRepetidaRequest(
        5,
        List.of(
            MetodoIntercambio.INTERCAMBIO,
            MetodoIntercambio.SUBASTA
        )
    );

    service.editarRepetida(coleccion.getId(), messi.getId(), request);

    FiguritaIntercambiable actualizada =
        repositorioColecciones.buscarRepetida(coleccion.getId(), messi.getId());

    assertTrue(
        actualizada.getMetodos().contains(MetodoIntercambio.INTERCAMBIO)
    );
    assertTrue(
        actualizada.getMetodos().contains(MetodoIntercambio.SUBASTA)
    );
  }

  @Test
  void editarRepetida_fallaSiIntentaEliminarMetodo() {
    FiguritaIntercambiable repetida = FiguritaIntercambiable.builder()
        .figurita(messi)
        .cantidadExistente(5)
        .cantidadReservada(0)
        .metodos(List.of(
            MetodoIntercambio.INTERCAMBIO,
            MetodoIntercambio.SUBASTA
        ))
        .build();

    coleccion.setRepetidas(List.of(repetida));
    repositorioColecciones.guardar(coleccion);

    EditarRepetidaRequest request = new EditarRepetidaRequest(
        5,
        List.of(MetodoIntercambio.SUBASTA)
    );

    assertThrows(
        BadRequestException.class,
        () -> service.editarRepetida(
            coleccion.getId(),
            messi.getId(),
            request
        )
    );
  }
}