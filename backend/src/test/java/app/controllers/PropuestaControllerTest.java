package app.controllers;

import app.model.entities.EstadoProceso;
import app.model.entities.EstadoPropuesta;
import app.model.entities.Figurita;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.Propuesta;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioPerfiles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PropuestaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean RepositorioPerfiles repoUser;
    @MockBean RepositorioFiguritas repoFigurita;
    @MockBean RepositorioPropuestas repoPropuesta;

    private List<MedioDeContacto> telegram(String numero) {
        return List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, numero));
    }

    private Perfil perfil(String id, String usuarioId, String handle) {
        return new Perfil(id, new Usuario(usuarioId, Rol.USUARIO), "", null, telegram(handle), null);
    }

    @Test
    void crearPropuestaDevuelve201() throws Exception {
        Perfil subastador    = perfil("1000", "u-1000", "@subastador");
        Perfil userPropuesta = perfil("1001", "u-1001", "@userPropuesta");

        Figurita buscada  = new Figurita("ARG-10", 2, null, null);
        Figurita ofrecida = new Figurita("FRA-10", 2, null, null);

        String json = """
        {
            "autor_id": "1000",
            "destinatario_id": "1001",
            "figurita_buscada_id": "ARG-10",
            "figuritas_ofrecidas_ids": ["FRA-10"]
        }
        """;

        when(repoUser.buscarPorId("1000")).thenReturn(subastador);
        when(repoUser.buscarPorId("1001")).thenReturn(userPropuesta);
        when(repoFigurita.buscarPorId("ARG-10")).thenReturn(buscada);
        when(repoFigurita.buscarPorId("FRA-10")).thenReturn(ofrecida);

        mockMvc.perform(post("/propuestas")
                .contentType("application/json")
                .content(json))
            .andExpect(status().isCreated());
    }

    @Test
    void aceptarPropuestaNoFalla() throws Exception {
        Perfil subastador    = perfil("1000", "u-1000", "@subastador");
        Perfil userPropuesta = perfil("1001", "u-1001", "@userPropuesta");

        Propuesta propuesta = new Propuesta("p-1", subastador, userPropuesta,
            new ArrayList<>(), null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))));

        when(repoPropuesta.buscarPorId("p-1")).thenReturn(propuesta);
        when(repoUser.buscarPorUsuarioId("u-1001")).thenReturn(userPropuesta);

        mockMvc.perform(patch("/propuestas/p-1/aceptar")
                .header("usuario_id", "u-1001"))
            .andExpect(status().isNoContent());
    }

    @Test
    void rechazarPropuestaNoFalla() throws Exception {
        Perfil subastador    = perfil("1000", "u-1000", "@subastador");
        Perfil userPropuesta = perfil("1001", "u-1001", "@userPropuesta");

        Propuesta propuesta = new Propuesta("p-2", subastador, userPropuesta,
            new ArrayList<>(), null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))));

        when(repoPropuesta.buscarPorId("p-2")).thenReturn(propuesta);
        when(repoUser.buscarPorUsuarioId("u-1001")).thenReturn(userPropuesta);

        mockMvc.perform(patch("/propuestas/p-2/rechazar")
                .header("usuario_id", "u-1001"))
            .andExpect(status().isNoContent());
    }

    @Test
    void aceptarPropuestaFallaConUsuarioIncorrecto() throws Exception {
        Perfil subastador    = perfil("1000", "u-1000", "@subastador");
        Perfil userPropuesta = perfil("1001", "u-1001", "@userPropuesta");
        Perfil otroUsuario   = perfil("1002", "u-1002", "@otro");

        Propuesta propuesta = new Propuesta("p-3", subastador, userPropuesta,
            new ArrayList<>(), null,
            new ArrayList<>(List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))));

        when(repoPropuesta.buscarPorId("p-3")).thenReturn(propuesta);
        when(repoUser.buscarPorUsuarioId("u-1002")).thenReturn(otroUsuario);

        mockMvc.perform(patch("/propuestas/p-3/aceptar")
                .header("usuario_id", "u-1002"))
            .andExpect(status().is(400));
    }
}