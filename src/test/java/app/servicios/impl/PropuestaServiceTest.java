package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.dto.PropuestaDto;
import app.dto.request.CrearPropuestaRequest;
import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.EstadoProceso;
import app.model.entities.Figurita;
import app.model.entities.Seleccion;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioUsuarios;
import app.servicios.INotificacionService;
import app.servicios.IPropuestaService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropuestaServiceTest {

  @Mock
  RepositorioPropuestas repositorioPropuestas;
  @Mock
  RepositorioUsuarios repositorioUsuarios;
  @Mock
  RepositorioFiguritas repositorioFiguritas;
  @Mock
  RepositorioFiguritasIntercambiables repositorioIntercambiables;
  @Mock
  INotificacionService notificacionesService;

  @InjectMocks
  PropuestaService propuestaService;

  Usuario lucas = new Usuario("1000", "Lucas", new Coleccion(), "+54911", new ArrayList<>());
  Usuario sofia = new Usuario("1001", "Sofía", new Coleccion(), "+54912", new ArrayList<>());
  Figurita messi = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);
  Figurita mbappe = new Figurita("FRA-10", 10, "Mbappé", Seleccion.FRANCIA);

  @Test
  void crearPropuestaDevuelveDto() {
    CrearPropuestaRequest request = new CrearPropuestaRequest(
        "1000", "1001", "ARG-10", List.of("FRA-10"));

    when(repositorioUsuarios.buscarPorId("1000")).thenReturn(lucas);
    when(repositorioUsuarios.buscarPorId("1001")).thenReturn(sofia);
    when(repositorioFiguritas.buscarPorId("ARG-10")).thenReturn(messi);
    when(repositorioFiguritas.buscarPorId("FRA-10")).thenReturn(mbappe);

    PropuestaDto resultado = propuestaService.crearPropuesta(request);

    assertEquals("1000", resultado.getUsuarioOrigenId());
    assertEquals("1001", resultado.getUsuarioDestinoId());
    assertEquals("ARG-10", resultado.getFiguritaBuscadaId());
    assertEquals(EstadoProceso.PENDIENTE, resultado.getEstado());
    verify(repositorioPropuestas).guardar(any());
  }

  @Test
  void crearPropuestaUsuarioOrigenNoExisteLanzaNotFoundException() {
    CrearPropuestaRequest request = new CrearPropuestaRequest(
        "9999", "1001", "ARG-10", List.of("FRA-10"));

    when(repositorioUsuarios.buscarPorId("9999")).thenReturn(null);

    assertThrows(NotFoundException.class,
        () -> propuestaService.crearPropuesta(request));
  }

  @Test
  void crearPropuestaUsuarioDestinoNoExisteLanzaNotFoundException() {
    CrearPropuestaRequest request = new CrearPropuestaRequest(
        "1000", "9999", "ARG-10", List.of("FRA-10"));

    when(repositorioUsuarios.buscarPorId("1000")).thenReturn(lucas);
    when(repositorioUsuarios.buscarPorId("9999")).thenReturn(null);

    assertThrows(NotFoundException.class,
        () -> propuestaService.crearPropuesta(request));
  }
}