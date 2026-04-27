package app.repositories.impl;

import app.model.entities.Coleccion;
import app.model.entities.EstadoProceso;
import app.model.entities.EstadoPropuesta;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.Propuesta;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Usuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioPropuestasEnMemoriaTest {

    private RepositorioPropuestasEnMemoria repositorio;
    private Perfil u1;
    private Perfil u2;
    private Perfil u3;

    private List<MedioDeContacto> telegram(String numero) {
        return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
    }

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioPropuestasEnMemoria();
        u1 = new Perfil("u-1",new Usuario("u-1000",  Rol.USUARIO), "Lucas",  new Coleccion(), telegram("@lucas"),  new ArrayList<>());
        u2 = new Perfil("u-2",new Usuario("u-1001",  Rol.USUARIO), "Sofía",  new Coleccion(), telegram("@sofia"),  new ArrayList<>());
        u3 = new Perfil("u-3",new Usuario("u-1002",  Rol.USUARIO), "Matías", new Coleccion(), telegram("@matias"), new ArrayList<>());
    }

    @Test
    void findByOrigenId_retornaSoloPropuestasDelOrigen() {
        Propuesta p1 = new Propuesta("p-1", u1, u2, new ArrayList<>(), null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))));
        Propuesta p2 = new Propuesta("p-2", u2, u3, new ArrayList<>(), null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))));

        repositorio.guardar(p1);
        repositorio.guardar(p2);

        List<Propuesta> resultado = repositorio.buscarPorAutorId("u-1");

        assertEquals(1, resultado.size());
        assertEquals("p-1", resultado.get(0).getId());
    }

    @Test
    void findByDestinoId_retornaSoloPropuestasDelDestino() {
        Propuesta p1 = new Propuesta("p-1", u1, u2, new ArrayList<>(), null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))));
        Propuesta p2 = new Propuesta("p-2", u2, u3, new ArrayList<>(), null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))));

        repositorio.guardar(p1);
        repositorio.guardar(p2);

        List<Propuesta> resultado = repositorio.buscarPorDestinatarioId("u-2");

        assertEquals(1, resultado.size());
        assertEquals("p-1", resultado.get(0).getId());
    }

    @Test
    void findByOrigenId_sinResultados_retornaListaVacia() {
        assertTrue(repositorio.buscarPorAutorId("u-99").isEmpty());
    }
}