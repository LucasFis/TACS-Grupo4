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

        String json = """
        {
            "figuritaId": "figu123",
            "duracion": 10
        }
        """;

        mockMvc.perform(post("/subastas")
                .header("id", "user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    void ofertarEnSubastaNoFalla() throws Exception {
        Usuario origen = new Usuario("user123", "", null, "", null);
        Usuario destino = new Usuario("user1", "", null, "", null);

        Figurita buscada = new Figurita("figu123", 2, null, null);
        Figurita ofrecida1 = new Figurita("figu321", 3, null, null);
        Figurita ofrecida2 = new Figurita("figu132", 4, null, null);

        Subasta subasta = new Subasta("1",null, null,null,null,null);

        subasta.setFiguritaSubastada(buscada);

        when(repoUser.findById("user123")).thenReturn(origen);
        when(repoUser.findById("user1")).thenReturn(destino);

        when(repoFigurita.findById("figu123")).thenReturn(buscada);
        when(repoFigurita.findById("figu321")).thenReturn(ofrecida1);
        when(repoFigurita.findById("figu132")).thenReturn(ofrecida2);

        when(repoSubasta.findById("1")).thenReturn(subasta);

        String json = """
    {
        "usuarioId": "user1",
        "figuritaBuscadaId": "figu123",
        "figuritasOfrecidas": ["figu321","figu132"]
    }
    """;

        mockMvc.perform(post("/subastas/1/propuestas")
                .header("id", "user123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }
}
