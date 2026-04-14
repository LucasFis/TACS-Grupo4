package app.servicios.impl;

import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Seleccion;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.model.notificador.Notificacion;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import app.repositories.impl.RepositorioNotificacionesEnMemoria;
import app.servicios.NotificacionService;
import app.servicios.SubastaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubastaServiceTest {
  @Mock
  private RepositorioUsuarios repositorioUsuarios;
  @Mock
  private RepositorioPropuestas repositorioPropuestas;
  @Mock
  private RepositorioSubastas repositorioSubastas;
  private RepositorioNotificaciones repositorioNotificaciones;
  @Mock
  private RepositorioFiguritas repositorioFiguritas;

  private SubastaService service;

  @BeforeEach
  void setUp() {
    this.repositorioNotificaciones = new RepositorioNotificacionesEnMemoria();
    NotificacionService serviceNotificacion = new NotificacionService(repositorioNotificaciones);

    service = new SubastaServiceImpl(repositorioSubastas, repositorioUsuarios,
        repositorioFiguritas, repositorioPropuestas,serviceNotificacion);
  }

  @Test
  void crearSubastaNotificaUsuarios() {

    Figurita messi = new Figurita("ARG-10", 10, "Messi",     Seleccion.ARGENTINA);
    Figurita diMaria   = new Figurita("ARG-11", 11, "Di María",  Seleccion.ARGENTINA);

    Coleccion coleccionMessi = new Coleccion();
    coleccionMessi.getFaltantes().add(messi);
    Usuario usuarioSinMessi = new Usuario("u-1", "Lucas", coleccionMessi, "+54911", new ArrayList<>());

    Coleccion coleccionRepetidos = new Coleccion();
    coleccionRepetidos.getRepetidas().add(new FiguritaIntercambiable(messi, 1, new ArrayList<>()));
    Usuario sofia = new Usuario("u-2", "Sofía", coleccionRepetidos, "+54911", new ArrayList<>());

    LocalDateTime fechaInicio = LocalDateTime.now();

    when(repositorioUsuarios.findById("u-2")).thenReturn(sofia);
    when(repositorioFiguritas.findById("ARG-10")).thenReturn(messi);

    when(repositorioUsuarios.buscarPorFiguritaFaltante(messi)).thenReturn(List.of(usuarioSinMessi));

    this.service.crearSubasta(sofia.getId(), fechaInicio, fechaInicio.plusMinutes(30),"ARG-10", null);

    assertEquals(1, repositorioNotificaciones.buscarPorUsuario(usuarioSinMessi).size());
  }

  @Test
  void crearSubastaNoNotificaUsuarios() {

    Figurita messi = new Figurita("ARG-10", 10, "Messi",     Seleccion.ARGENTINA);
    Figurita diMaria   = new Figurita("ARG-11", 11, "Di María",  Seleccion.ARGENTINA);

    Coleccion coleccionMessi = new Coleccion();
    coleccionMessi.getFaltantes().add(messi);
    Usuario usuarioSinMessi = new Usuario("u-1", "Lucas", coleccionMessi, "+54911", new ArrayList<>());

    Coleccion coleccionRepetidos = new Coleccion();
    coleccionRepetidos.getRepetidas().add(new FiguritaIntercambiable(messi, 1, new ArrayList<>()));
    Usuario sofia = new Usuario("u-2", "Sofía", coleccionRepetidos, "+54911", new ArrayList<>());

    LocalDateTime fechaInicio = LocalDateTime.now();

    when(repositorioUsuarios.findById("u-2")).thenReturn(sofia);
    when(repositorioFiguritas.findById("ARG-11")).thenReturn(diMaria);

    when(repositorioUsuarios.buscarPorFiguritaFaltante(diMaria)).thenReturn(List.of());

    this.service.crearSubasta(sofia.getId(), fechaInicio, fechaInicio.plusMinutes(30),"ARG-11", null);

    assertEquals(0, repositorioNotificaciones.buscarPorUsuario(usuarioSinMessi).size());
  }
}
