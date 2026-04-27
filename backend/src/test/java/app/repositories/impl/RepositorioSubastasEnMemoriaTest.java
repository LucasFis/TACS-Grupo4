package app.repositories.impl;

import app.model.entities.Coleccion;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.Rol;
import app.model.entities.Subasta;
import app.model.entities.Perfil;
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
    private Perfil p1;
    private Perfil p2;

    private List<MedioDeContacto> telegram(String numero) {
        return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
    }

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioSubastasEnMemoria();
        p1 = new Perfil("1", new Usuario("u-1000", Rol.USUARIO), "Lucas", new Coleccion(), telegram("@lucas"), new ArrayList<>());
        p2 = new Perfil("2", new Usuario("u-1001", Rol.USUARIO), "Sofía", new Coleccion(), telegram("@sofia"), new ArrayList<>());
    }

    @Test
    void findByUsuarioId_retornaSoloSubastasDelUsuario() {
        Subasta s1 = new Subasta("s-1", p1,
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2), null);
        Subasta s2 = new Subasta("s-2", p2,
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2), null);

        repositorio.guardar(s1);
        repositorio.guardar(s2);

        List<Subasta> resultado = repositorio.buscarPorPerfilId("1");

        assertEquals(1, resultado.size());
        assertEquals("s-1", resultado.get(0).getId());
    }

    @Test
    void findByUsuarioId_sinResultados_retornaListaVacia() {
        assertTrue(repositorio.buscarPorPerfilId("u-99").isEmpty());
    }
}
