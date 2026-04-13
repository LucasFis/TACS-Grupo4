package app.model.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubastaTest {

    private final Usuario usuario = new Usuario("u-1", "Lucas", new Coleccion(), "+54911", new ArrayList<>());

    @Test
    void estaActivo_cuandoSubastaActiva_retornaTrue() {
        Subasta subasta = new Subasta("s-1", usuario,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(2),
                null, null);

        assertTrue(subasta.estaActivo());
    }

    @Test
    void estaActivo_cuandoSubastaVencida_retornaFalse() {
        Subasta subasta = new Subasta("s-2", usuario,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                null, null);

        assertFalse(subasta.estaActivo());
    }

    @Test
    void estaActivo_cuandoSubastaNoIniciada_retornaFalse() {
        Subasta subasta = new Subasta("s-3", usuario,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                null, null);

        assertFalse(subasta.estaActivo());
    }

    @Test
    void algoritmoSeleccionador_noLanzaExcepcion() {
        Subasta subasta = new Subasta("s-4", usuario,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusDays(2),
                null, null);

        subasta.algoritmoSeleccionador(null);
    }
}