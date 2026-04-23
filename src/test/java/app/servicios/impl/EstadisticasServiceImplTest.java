package app.servicios.impl;

import app.dto.EstadisticasDto;
import app.model.entities.Coleccion;
import app.model.entities.FiguritaIntercambiable;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadisticasServiceImplTest {

    @Mock
    private RepositorioUsuarios repositorioUsuarios;
    @Mock
    private RepositorioPropuestas repositorioPropuestas;
    @Mock
    private RepositorioSubastas repositorioSubastas;

    private EstadisticasServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EstadisticasServiceImpl(repositorioUsuarios, repositorioPropuestas, repositorioSubastas);
    }

    @Test
    void getEstadisticas_sinDatos_retornaTodosCeros() {
        when(repositorioUsuarios.contar()).thenReturn(0);
        when(repositorioUsuarios.buscarTodos()).thenReturn(List.of());
        when(repositorioPropuestas.contar()).thenReturn(0);
        when(repositorioSubastas.buscarTodos()).thenReturn(List.of());

        EstadisticasDto resultado = service.obtenerEstadisticas();

        assertEquals(0, resultado.getTotalUsuarios());
        assertEquals(0, resultado.getTotalFiguritasPublicadas());
        assertEquals(0, resultado.getTotalPropuestas());
        assertEquals(0, resultado.getTotalSubastasActivas());
    }

    @Test
    void getEstadisticas_conDatos_retornaValoresCorrectos() {
        Coleccion coleccionConDos = new Coleccion();
        coleccionConDos.getRepetidas().add(new FiguritaIntercambiable(null, 1, new ArrayList<>()));
        coleccionConDos.getRepetidas().add(new FiguritaIntercambiable(null, 2, new ArrayList<>()));

        Coleccion coleccionConUna = new Coleccion();
        coleccionConUna.getRepetidas().add(new FiguritaIntercambiable(null, 3, new ArrayList<>()));

        Usuario u1 = new Usuario("u-1", "Lucas", coleccionConDos, "+54911", new ArrayList<>());
        Usuario u2 = new Usuario("u-2", "Sofía", coleccionConUna, "+54911", new ArrayList<>());

        Subasta subastaActiva = new Subasta("s-1", u1,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2), null, null);

        when(repositorioUsuarios.contar()).thenReturn(2);
        when(repositorioUsuarios.buscarTodos()).thenReturn(List.of(u1, u2));
        when(repositorioPropuestas.contar()).thenReturn(4);
        when(repositorioSubastas.buscarTodos()).thenReturn(List.of(subastaActiva));

        EstadisticasDto resultado = service.obtenerEstadisticas();

        assertEquals(2, resultado.getTotalUsuarios());
        assertEquals(3, resultado.getTotalFiguritasPublicadas());
        assertEquals(4, resultado.getTotalPropuestas());
        assertEquals(1, resultado.getTotalSubastasActivas());
    }

    @Test
    void getEstadisticas_filtraSoloSubastasActivas() {
        Usuario u1 = new Usuario("u-1", "Lucas", new Coleccion(), "+54911", new ArrayList<>());

        Subasta activa   = new Subasta("s-1", u1,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2), null, null);
        Subasta vencida  = new Subasta("s-2", u1,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), null, null);

        when(repositorioUsuarios.contar()).thenReturn(1);
        when(repositorioUsuarios.buscarTodos()).thenReturn(List.of(u1));
        when(repositorioPropuestas.contar()).thenReturn(0);
        when(repositorioSubastas.buscarTodos()).thenReturn(List.of(activa, vencida));

        EstadisticasDto resultado = service.obtenerEstadisticas();

        assertEquals(1, resultado.getTotalSubastasActivas());
    }
}