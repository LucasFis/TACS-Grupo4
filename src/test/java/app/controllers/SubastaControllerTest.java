package app.controllers;

import app.model.entities.Figurita;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubastaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RepositorioUsuarios repoUser;

    @MockBean
    RepositorioSubastas repoSubasta;

    @MockBean
    RepositorioFiguritas repoFigurita;

    @Test
    void crearSubastaNoFalla() throws Exception {
        Usuario origen = new Usuario("user123", "", null, "", null);
        Figurita figuSubastada = new Figurita("figu123", 2, null, null);

        String json = """
        {
            "figurita_id": "figu123",
            "duracion": 10
        }
        """;

        when(repoUser.buscarPorId("user123")).thenReturn(origen);
        when(repoFigurita.buscarPorId("figu123")).thenReturn(figuSubastada);

        mockMvc.perform(post("/subastas")
                .header("user_id", "user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    void ofertarEnSubastaNoFalla() throws Exception {
        Usuario subastador = new Usuario("userSubastador", "", null, "", null);
        Usuario userPropuesta = new Usuario("userPropuesta", "", null, "", null);

        LocalDateTime fechaInicio = LocalDateTime.now();

        Figurita buscada = new Figurita("figu123", 2, null, null);
        Figurita ofrecida1 = new Figurita("figu321", 3, null, null);
        Figurita ofrecida2 = new Figurita("figu132", 4, null, null);

        Subasta subasta = new Subasta("1",subastador, fechaInicio,fechaInicio.plusMinutes(30),buscada,null);

        subasta.setFiguritaSubastada(buscada);

        when(repoUser.buscarPorId("userSubastador")).thenReturn(subastador);
        when(repoUser.buscarPorId("userPropuesta")).thenReturn(userPropuesta);

        when(repoFigurita.buscarPorId("figu123")).thenReturn(buscada);
        when(repoFigurita.buscarPorId("figu321")).thenReturn(ofrecida1);
        when(repoFigurita.buscarPorId("figu132")).thenReturn(ofrecida2);

        when(repoSubasta.buscarPorId("1")).thenReturn(subasta);

        String json = """
    {
        "usuario_id": "userSubastador",
        "figuritas_ofrecidas": ["figu321","figu132"]
    }
    """;

        mockMvc.perform(post("/subastas/1/propuestas")
                .header("user_id", "userPropuesta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }
}
