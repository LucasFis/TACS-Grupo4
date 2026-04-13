package app.repositories.impl;

import app.model.entities.Coleccion;
import app.model.entities.EstadoProceso;
import app.model.entities.Propuesta;
import app.model.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioPropuestasEnMemoriaTest {

    private RepositorioPropuestasEnMemoria repositorio;
    private Usuario u1;
    private Usuario u2;
    private Usuario u3;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioPropuestasEnMemoria();
        u1 = new Usuario("u-1", "Lucas",  new Coleccion(), "+541", new ArrayList<>());
        u2 = new Usuario("u-2", "Sofía",  new Coleccion(), "+542", new ArrayList<>());
        u3 = new Usuario("u-3", "Matías", new Coleccion(), "+543", new ArrayList<>());
    }

    @Test
    void findByOrigenId_retornaSoloPropuestasDelOrigen() {
        Propuesta p1 = new Propuesta("p-1", u1, u2, new ArrayList<>(), null, EstadoProceso.PENDIENTE);
        Propuesta p2 = new Propuesta("p-2", u2, u3, new ArrayList<>(), null, EstadoProceso.PENDIENTE);
        repositorio.save(p1);
        repositorio.save(p2);

        List<Propuesta> resultado = repositorio.findByOrigenId("u-1");

        assertEquals(1, resultado.size());
        assertEquals("p-1", resultado.get(0).getId());
    }

    @Test
    void findByDestinoId_retornaSoloPropuestasDelDestino() {
        Propuesta p1 = new Propuesta("p-1", u1, u2, new ArrayList<>(), null, EstadoProceso.PENDIENTE);
        Propuesta p2 = new Propuesta("p-2", u2, u3, new ArrayList<>(), null, EstadoProceso.PENDIENTE);
        repositorio.save(p1);
        repositorio.save(p2);

        List<Propuesta> resultado = repositorio.findByDestinoId("u-2");

        assertEquals(1, resultado.size());
        assertEquals("p-1", resultado.get(0).getId());
    }

    @Test
    void findByOrigenId_sinResultados_retornaListaVacia() {
        assertTrue(repositorio.findByOrigenId("u-99").isEmpty());
    }
}