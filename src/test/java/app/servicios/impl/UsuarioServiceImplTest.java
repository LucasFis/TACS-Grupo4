package app.servicios.impl;

import app.dto.OperacionesDto;
import app.model.entities.Coleccion;
import app.model.entities.EstadoProceso;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private RepositorioUsuarios repositorioUsuarios;
    @Mock
    private RepositorioPropuestas repositorioPropuestas;
    @Mock
    private RepositorioSubastas repositorioSubastas;

    private UsuarioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioServiceImpl(repositorioUsuarios, repositorioPropuestas, repositorioSubastas);
    }

    @Test
    void getOperacionesUsuario_usuarioInexistente_retornaNull() {
        when(repositorioUsuarios.findById("u-99")).thenReturn(null);

        assertNull(service.getOperacionesUsuario("u-99"));
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

        when(repositorioUsuarios.findById("u-1")).thenReturn(usuario);
        when(repositorioPropuestas.findByOrigenId("u-1")).thenReturn(enviadas);
        when(repositorioPropuestas.findByDestinoId("u-1")).thenReturn(recibidas);
        when(repositorioSubastas.findByUsuarioId("u-1")).thenReturn(subastas);

        OperacionesDto resultado = service.getOperacionesUsuario("u-1");

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

        when(repositorioUsuarios.findById("u-1")).thenReturn(usuario);
        when(repositorioPropuestas.findByOrigenId("u-1")).thenReturn(new ArrayList<>());
        when(repositorioPropuestas.findByDestinoId("u-1")).thenReturn(new ArrayList<>());
        when(repositorioSubastas.findByUsuarioId("u-1")).thenReturn(subastas);

        OperacionesDto resultado = service.getOperacionesUsuario("u-1");

        assertEquals(1, resultado.getSubastasActivas().size());
        assertEquals("s-1", resultado.getSubastasActivas().get(0).getId());
    }
}
