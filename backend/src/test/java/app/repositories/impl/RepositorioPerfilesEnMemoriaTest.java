package app.repositories.impl;

import app.exceptions.NotFoundException;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.Rol;
import app.model.entities.Seleccion;
import app.model.entities.Perfil;
import app.model.entities.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
class RepositorioPerfilesEnMemoriaTest {

    private RepositorioPerfilesEnMemoria repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioPerfilesEnMemoria();
    }

    private List<MedioDeContacto> telegram(String numero) {
        return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
    }

    @Test
    void save_y_findById_retornaPerfil() {
        Perfil perfil = new Perfil("u-1",new Usuario("u-1000",  Rol.USUARIO), "Lucas", new Coleccion(), telegram("@lucas"), new ArrayList<>());

        repositorio.guardar(perfil);

        assertEquals(perfil, repositorio.buscarPorId("u-1"));
    }

    @Test
    void findById_inexistente_retornaNull() {
        assertThrows(NotFoundException.class, () -> repositorio.buscarPorId("inexistente"));
    }

    @Test
    void buscarPorFiguritaFaltanteDevuelve2() {
        Perfil perfil  = new Perfil("u-1", new Usuario("u-1000", Rol.USUARIO), "Lucas",    new Coleccion(), telegram("@lucas"),    new ArrayList<>());
        Perfil perfil2 = new Perfil("u-2", new Usuario("u-1001", Rol.USUARIO), "Juan",     new Coleccion(), telegram("@juan"),     new ArrayList<>());
        Perfil perfil3 = new Perfil("u-4", new Usuario("u-1002", Rol.USUARIO), "Cristina", new Coleccion(), telegram("@cristina"), new ArrayList<>());

        Figurita messi   = new Figurita("ARG-10", 10, "Messi",   Seleccion.ARGENTINA);
        Figurita diMaria = new Figurita("ARG-11", 11, "Di María", Seleccion.ARGENTINA);

        perfil.getColeccion().agregarFaltante(messi);
        perfil.getColeccion().agregarFaltante(diMaria);

        perfil2.getColeccion().agregarFaltante(messi);
        perfil2.getColeccion().agregarFaltante(diMaria);

        perfil3.getColeccion().agregarFaltante(diMaria);

        repositorio.guardar(perfil);
        repositorio.guardar(perfil2);
        repositorio.guardar(perfil3);

        assertEquals(2, repositorio.buscarPorFiguritaFaltante(messi).size());
    }
}