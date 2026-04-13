package app.repositories.impl;

import app.model.entities.Coleccion;
import app.model.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RepositorioUsuariosEnMemoriaTest {

    private RepositorioUsuariosEnMemoria repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioUsuariosEnMemoria();
    }

    @Test
    void save_y_findById_retornaUsuario() {
        Usuario usuario = new Usuario("u-1", "Lucas", new Coleccion(), "+5491100000001", new ArrayList<>());

        repositorio.save(usuario);

        assertEquals(usuario, repositorio.findById("u-1"));
    }

    @Test
    void findById_inexistente_retornaNull() {
        assertNull(repositorio.findById("inexistente"));
    }
}
