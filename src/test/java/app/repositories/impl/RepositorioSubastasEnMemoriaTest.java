package app.repositories.impl;

import app.model.entities.Coleccion;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioSubastasEnMemoriaTest {

    private RepositorioSubastasEnMemoria repositorio;
    private Usuario u1;
    private Usuario u2;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioSubastasEnMemoria();
        u1 = new Usuario("u-1", "Lucas", new Coleccion(), "+541", new ArrayList<>());
        u2 = new Usuario("u-2", "Sofía", new Coleccion(), "+542", new ArrayList<>());
    }

    @Test
    void findByUsuarioId_retornaSoloSubastasDelUsuario() {
        Subasta s1 = new Subasta("s-1", u1,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2),
                null, null);
        Subasta s2 = new Subasta("s-2", u2,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2),
                null, null);
        repositorio.save(s1);
        repositorio.save(s2);

        List<Subasta> resultado = repositorio.findByUsuarioId("u-1");

        assertEquals(1, resultado.size());
        assertEquals("s-1", resultado.get(0).getId());
    }

    @Test
    void findByUsuarioId_sinResultados_retornaListaVacia() {
        assertTrue(repositorio.findByUsuarioId("u-99").isEmpty());
    }
}
