package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.exceptions.FiguritaDuplicadaException;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Seleccion;
import app.model.entities.Usuario;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;
import app.servicios.INotificacionService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ColeccionServiceTest {

  @Mock
  private RepositorioColecciones repositorioColecciones;
  @Mock private RepositorioFiguritas repositorioFiguritas;
  @Mock private RepositorioPerfiles repositorioPerfiles;
  @Mock private INotificacionService notificacionService;

  private ColeccionService service;

  private Perfil lucas;
  private Figurita messi;
  private Coleccion coleccion;

  @BeforeEach
  void setUp() {
    service = new ColeccionService(repositorioFiguritas, repositorioColecciones,
        repositorioPerfiles, notificacionService);

    messi = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);

    coleccion = new Coleccion();
    coleccion.setId("col-1");

    lucas = new Perfil("1", new Usuario("u-1", Rol.USUARIO), "Lucas",
        coleccion, List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@lucas")), new ArrayList<>());
  }

  @Test
  void agregarFaltante_agregaFiguritaAColeccion() {
    when(repositorioColecciones.buscarPorId("col-1")).thenReturn(coleccion);
    when(repositorioFiguritas.buscarPorId("ARG-10")).thenReturn(messi);

    service.agregarFaltante("col-1", "ARG-10");

    assertEquals(1, coleccion.getFaltantes().size());
    assertEquals(messi, coleccion.getFaltantes().get(0));
    verify(repositorioColecciones).guardar(coleccion);
  }

  @Test
  void agregarFaltante_figuritaDuplicada_lanzaExcepcion() {
    coleccion.getFaltantes().add(messi);

    when(repositorioColecciones.buscarPorId("col-1")).thenReturn(coleccion);
    when(repositorioFiguritas.buscarPorId("ARG-10")).thenReturn(messi);

    assertThrows(FiguritaDuplicadaException.class,
        () -> service.agregarFaltante("col-1", "ARG-10"));
  }

  @Test
  void agregarRepetida_agregaFiguritaYNotificaInteresados() {
    Perfil interesado = new Perfil("2", new Usuario("u-2", Rol.USUARIO), "Sofía",
        new Coleccion(), List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@sofia")), new ArrayList<>());

    when(repositorioColecciones.buscarPorId("col-1")).thenReturn(coleccion);
    when(repositorioFiguritas.buscarPorId("ARG-10")).thenReturn(messi);
    when(repositorioPerfiles.buscarPorUsuarioId("u-1")).thenReturn(lucas);
    when(repositorioPerfiles.buscarPorFiguritaFaltante(messi)).thenReturn(List.of(interesado));

    service.agregarRepetida("col-1", "u-1", "ARG-10", 2, List.of("INTERCAMBIO"));

    assertEquals(1, coleccion.getRepetidas().size());
    verify(repositorioColecciones).guardar(coleccion);
    verify(notificacionService).notificarInteresados(eq(List.of(interesado)), anyString());
  }

  @Test
  void agregarRepetida_sinInteresados_noNotifica() {
    when(repositorioColecciones.buscarPorId("col-1")).thenReturn(coleccion);
    when(repositorioFiguritas.buscarPorId("ARG-10")).thenReturn(messi);
    when(repositorioPerfiles.buscarPorUsuarioId("u-1")).thenReturn(lucas);
    when(repositorioPerfiles.buscarPorFiguritaFaltante(messi)).thenReturn(List.of());

    service.agregarRepetida("col-1", "u-1", "ARG-10", 2, List.of("INTERCAMBIO"));

    verify(notificacionService).notificarInteresados(eq(List.of()), anyString());
  }
}