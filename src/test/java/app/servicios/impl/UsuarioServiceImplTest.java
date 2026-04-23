package app.servicios.impl;

import app.dto.FiguritaIntercambiableDto;
import app.dto.OperacionesDto;
import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.EstadoProceso;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Seleccion;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private RepositorioUsuarios repositorioUsuarios;
    @Mock
    private RepositorioPropuestas repositorioPropuestas;
    @Mock
    private RepositorioSubastas repositorioSubastas;
    @Mock
    private RepositorioNotificaciones repositorioNotificaciones;

    @Mock
    private RepositorioFiguritasIntercambiables repositorioFiguritasIntercambiables;

    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(repositorioUsuarios, repositorioPropuestas,
            repositorioSubastas, repositorioNotificaciones, repositorioFiguritasIntercambiables);
    }

    @Test
    void getOperacionesUsuario_usuarioInexistente_retornaNull() {
        when(repositorioUsuarios.buscarPorId("u-99")).thenReturn(null);

        assertNull(service.obtenerOperacionesUsuario("u-99"));
    }

    @Test
    void getOperacionesUsuario_usuarioExistente_retornaOperaciones() {
        Coleccion coleccion = new Coleccion();
        coleccion.getRepetidas().add(new FiguritaIntercambiable(null, 1, new ArrayList<>()));
        Usuario usuario = new Usuario("u-1", "Lucas", coleccion, "+54911", new ArrayList<>());

        Usuario sofia = new Usuario("u-2", "Sofía", new Coleccion(), "+54911", new ArrayList<>());
        List<Propuesta> enviadas  = List.of(new Propuesta("p-1", usuario, sofia,   new ArrayList<>(), null, EstadoProceso.PENDIENTE));
        List<Propuesta> recibidas = List.of(new Propuesta("p-2", sofia,   usuario, new ArrayList<>(), null, EstadoProceso.RECHAZADO));
        List<Subasta>   subastas  = List.of(new Subasta("s-1", usuario,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2), null, null));

        when(repositorioUsuarios.buscarPorId("u-1")).thenReturn(usuario);
        when(repositorioPropuestas.buscarPorOrigenId("u-1")).thenReturn(enviadas);
        when(repositorioPropuestas.buscarPorDestinoId("u-1")).thenReturn(recibidas);
        when(repositorioSubastas.buscarPorUsuarioId("u-1")).thenReturn(subastas);

        OperacionesDto resultado = service.obtenerOperacionesUsuario("u-1");

        assertEquals(1, resultado.getFiguritasPublicadas().size());
        assertEquals(1, resultado.getPropuestasEnviadas().size());
        assertEquals(1, resultado.getPropuestasRecibidas().size());
        assertEquals(1, resultado.getSubastasActivas().size());
    }

    @Test
    void getOperacionesUsuario_filtraSoloSubastasActivas() {
        Usuario usuario = new Usuario("u-1", "Lucas", new Coleccion(), "+54911", new ArrayList<>());

        List<Subasta> subastas = List.of(
                new Subasta("s-1", usuario,
                        LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2), null, null),
                new Subasta("s-2", usuario,
                        LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), null, null)
        );

        when(repositorioUsuarios.buscarPorId("u-1")).thenReturn(usuario);
        when(repositorioPropuestas.buscarPorOrigenId("u-1")).thenReturn(new ArrayList<>());
        when(repositorioPropuestas.buscarPorDestinoId("u-1")).thenReturn(new ArrayList<>());
        when(repositorioSubastas.buscarPorUsuarioId("u-1")).thenReturn(subastas);

        OperacionesDto resultado = service.obtenerOperacionesUsuario("u-1");

        assertEquals(1, resultado.getSubastasActivas().size());
        assertEquals("s-1", resultado.getSubastasActivas().get(0).getId());
    }

    @Test
    void getIntercambiablesUsuario_usuarioExistente_retornaLista() {
        Usuario usuario = new Usuario("u-1", "Lucas", new Coleccion(), "+54911", new ArrayList<>());

        Figurita figurita = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);
        FiguritaIntercambiable fi =
            new FiguritaIntercambiable(figurita, 2, new ArrayList<>(), "u-1");

        when(repositorioUsuarios.buscarPorId("u-1")).thenReturn(usuario);
        when(repositorioFiguritasIntercambiables.buscarPorUsuarioId("u-1"))
            .thenReturn(List.of(fi));

        List<FiguritaIntercambiableDto> resultado =
            service.obtenerIntercambiablesUsuario("u-1");

        assertEquals(1, resultado.size());
        assertEquals("ARG-10", resultado.get(0).getFiguritaId());
    }

    @Test
    void getIntercambiablesUsuario_usuarioInexistente_lanzaNotFoundException() {
        when(repositorioUsuarios.buscarPorId("u-99")).thenReturn(null);

        assertThrows(NotFoundException.class,
            () -> service.obtenerIntercambiablesUsuario("u-99"));
    }
    @Test
    void agregarCalificacion_valida_retornaPromedio() {
        Usuario usuario = new Usuario("u-1", "Lucas", new Coleccion(), "+54911", new ArrayList<>());

        // ya tiene una calificación previa
        usuario.getCalificaciones().add(6);

        when(repositorioUsuarios.buscarPorId("u-1")).thenReturn(usuario);

        Number resultado = service.agregarCalificacion(8, "u-1");

        // promedio de 6 y 8 = 7
        assertEquals(7.0, resultado.doubleValue());
        verify(repositorioUsuarios).guardar(usuario);
    }


}
