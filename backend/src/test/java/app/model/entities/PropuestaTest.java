package app.model.entities;

import app.exceptions.PropuestaException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PropuestaTest {

    private Perfil origen;
    private Perfil destino;
    private Propuesta propuesta;

    @BeforeEach
    void setUp() {
        origen  = new Perfil("1", new Usuario("u-1", Rol.USUARIO), "Origen",  null, List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@origen")),  List.of());
        destino = new Perfil("2", new Usuario("u-2", Rol.USUARIO), "Destino", null, List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@destino")), List.of());

        propuesta = new Propuesta(
            "123",
            origen,
            destino,
            List.of(),
            null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE)))
        );
    }

    @Test
    void deberiaAceptarPropuestaPendiente() {
        propuesta.aceptar(destino);

        assertEquals(EstadoProceso.ACEPTADO, propuesta.obtenerEstadoActual().getValor());
    }

    @Test
    void deberiaRechazarPropuestaPendiente() {
        propuesta.rechazar(destino);

        assertEquals(EstadoProceso.RECHAZADO, propuesta.obtenerEstadoActual().getValor());
    }

    @Test
    void noDeberiaAceptarUnaPropuestaYaAceptada() {
        propuesta.aceptar(destino);

        assertThrows(PropuestaException.class, () -> propuesta.aceptar(destino));
    }

    @Test
    void noDeberiaRechazarUnaPropuestaYaAceptada() {
        propuesta.aceptar(destino);

        assertThrows(PropuestaException.class, () -> propuesta.rechazar(destino));
    }

    @Test
    void noDeberiaAceptarSiNoEsElUsuarioDestino() {
        Perfil otro = new Perfil("3", new Usuario("u-3", Rol.USUARIO), "Otro", null, List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@otro")), List.of());

        assertThrows(PropuestaException.class, () -> propuesta.aceptar(otro));
    }
}