package app.controllers;

import app.model.entities.Figurita;
import app.model.entities.Propuesta;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
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

    @MockBean
    RepositorioUsuarios repoUser;

    @MockBean
    RepositorioFiguritas repoFigurita;
    @MockBean
    RepositorioPropuestas repoPropuesta;

    @Test
    void crearPropuestaDevuelve201() throws Exception {
        Usuario subastador = new Usuario("1000", "", null, "", null);
        Usuario userPropuesta = new Usuario("1001", "", null, "", null);

        Figurita buscada = new Figurita("ARG-10", 2, null, null);
        Figurita ofrecida = new Figurita("FRA-10", 2, null, null);

        String json = """
        {
            "usuario_origen_id": "1000",
            "usuario_destino_id": "1001",
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
        Usuario subastador = new Usuario("1000", "", null, "", null);
        Usuario userPropuesta = new Usuario("1001", "", null, "", null);
        Propuesta propuesta = new Propuesta(subastador, userPropuesta, null, null, null);

        propuesta.setId("1000");

        when(repoPropuesta.buscarPorId("1000")).thenReturn(propuesta);

        mockMvc.perform(patch("/propuestas/1000/aceptar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rechazarPropuestaNoFalla() throws Exception {
        Usuario subastador = new Usuario("1000", "", null, "", null);
        Usuario userPropuesta = new Usuario("1001", "", null, "", null);
        Propuesta propuesta = new Propuesta(subastador, userPropuesta, null, null, null);

        propuesta.setId("1000");

        when(repoPropuesta.buscarPorId("1000")).thenReturn(propuesta);

        mockMvc.perform(patch("/propuestas/1000/rechazar"))
                .andExpect(status().isNoContent());
    }
}