package app.model.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PropuestaTest {

    private Usuario origen;
    private Usuario destino;
    private Propuesta propuesta;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void deberiaAceptarPropuestaPendiente() {
        propuesta.aceptar(destino);

        assertEquals(EstadoProceso.ACEPTADO, propuesta.getEstado());
    }

    @Test
    void deberiaRechazarPropuestaPendiente() {
        propuesta.rechazar(destino);

        assertEquals(EstadoProceso.RECHAZADO, propuesta.getEstado());
    }

    @Test
    void noDeberiaAceptarUnaPropuestaYaAceptada() {
        propuesta.aceptar(destino);

        assertThrows(RuntimeException.class, () -> {
            propuesta.aceptar(destino);
        });
    }

    @Test
    void noDeberiaRechazarUnaPropuestaYaAceptada() {
        propuesta.aceptar(destino);

        assertThrows(RuntimeException.class, () -> {
            propuesta.rechazar(destino);
        });
    }

    @Test
    void noDeberiaAceptarSiNoEsElUsuarioDestino() {
        Usuario otro = new Usuario("3", "Otro", null, "", List.of());

        assertThrows(RuntimeException.class, () -> {
            propuesta.aceptar(otro);
        });
    }
}