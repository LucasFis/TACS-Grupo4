package app.model.entities;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubastaTest {

    private Perfil usuario;

    @BeforeEach
    void setUp() {
        usuario = new Perfil("u-1", new Usuario("u-1000", Rol.USUARIO), "Lucas", new Coleccion(),
            List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@lucas")), new ArrayList<>());
    }

    @Test
    void estaActivo_cuandoSubastaActiva_retornaTrue() {
        Subasta subasta = new Subasta("s-1", usuario,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(2),
            null);

        assertTrue(subasta.estaActivo());
    }

    @Test
    void estaActivo_cuandoSubastaVencida_retornaFalse() {
        Subasta subasta = new Subasta("s-2", usuario,
            LocalDateTime.now().minusDays(3),
            LocalDateTime.now().minusDays(1),
            null);

        assertFalse(subasta.estaActivo());
    }

    @Test
    void estaActivo_cuandoSubastaNoIniciada_retornaFalse() {
        Subasta subasta = new Subasta("s-3", usuario,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(3),
            null);

        assertFalse(subasta.estaActivo());
    }
}