package app.repositories.impl;

import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.Seleccion;
import app.model.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioUsuariosEnMemoriaTest {

    private RepositorioUsuariosEnMemoria repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioUsuariosEnMemoria();
    }

    @Test
    void save_y_findById_retornaUsuario() {
        Usuario usuario = new Usuario("u-1", "Lucas", new Coleccion(), "+5491100000001", new ArrayList<>());

        repositorio.guardar(usuario);

        assertEquals(usuario, repositorio.buscarPorId("u-1"));
    }

    @Test
    void findById_inexistente_retornaNull() {
        assertThrows(NotFoundException.class, () -> {
            repositorio.buscarPorId("inexistente");
        });
    }

    @Test
    void buscarPorFiguritaFaltanteDevuelve2() {
        Usuario usuario = new Usuario("u-1", "Lucas", new Coleccion(), "+5491100000001", new ArrayList<>());
        Usuario usuario2 = new Usuario("u-2", "Juan", new Coleccion(), "+5491100000001", new ArrayList<>());
        Usuario usuario3 = new Usuario("u-4", "Cristina", new Coleccion(), "+5491100000001", new ArrayList<>());

        Figurita messi = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);
        Figurita diMaria = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);

        usuario.getColeccion().agregarFaltante(messi);
        usuario.getColeccion().agregarFaltante(diMaria);

        usuario2.getColeccion().agregarFaltante(messi);
        usuario2.getColeccion().agregarFaltante(diMaria);

        usuario3.getColeccion().agregarFaltante(diMaria);

        repositorio.guardar(usuario);
        repositorio.guardar(usuario2);
        repositorio.guardar(usuario3);

        assertEquals(2, repositorio.buscarPorFiguritaFaltante(messi).size());

    }
}
