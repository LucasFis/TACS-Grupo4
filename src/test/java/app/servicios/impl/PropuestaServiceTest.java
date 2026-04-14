package app.servicios.impl;

import app.model.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PropuestaServiceTest {

    private PropuestaService service;
    private Usuario origen;
    private Usuario destino;
    private Propuesta propuesta;

    @BeforeEach
    void setUp() {
        service = new PropuestaService();

        origen = new Usuario("1", "Origen", null, "", List.of());
        destino = new Usuario("2", "Destino", null, "", List.of());

        propuesta = new Propuesta(
                "123",
                origen,
                destino,
                List.of(),
                null,
                EstadoProceso.PENDIENTE
        );

        service.crear(propuesta);
    }

    @Test
    void deberiaCrearYObtenerPropuesta() {
        Propuesta obtenida = service.obtenerPorId("123");

        assertNotNull(obtenida);
        assertEquals("123", obtenida.getId());
    }

    @Test
    void deberiaFallarSiLaPropuestaNoExiste() {
        assertThrows(RuntimeException.class, () -> {
            service.obtenerPorId("999");
        });
    }

    @Test
    void deberiaAceptarPropuesta() {
        service.aceptar("123");

        Propuesta actualizada = service.obtenerPorId("123");

        assertEquals(EstadoProceso.ACEPTADO, actualizada.getEstado());
    }

    @Test
    void noDeberiaAceptarDosVeces() {
        service.aceptar("123");

        assertThrows(RuntimeException.class, () -> {
            service.aceptar("123");
        });
    }

    @Test
    void noDeberiaAceptarPropuestaInexistente() {
        assertThrows(RuntimeException.class, () -> {
            service.aceptar("999");
        });
    }

    @Test
    void noDeberiaRechazarPropuestaInexistente() {
        assertThrows(RuntimeException.class, () -> {
            service.rechazar("999");
        });
    }

    @Test
    void noDeberiaRechazarLuegoDeAceptar() {
        service.aceptar("123");

        assertThrows(RuntimeException.class, () -> {
            service.rechazar("123");
        });
    }
}