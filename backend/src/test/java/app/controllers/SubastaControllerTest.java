package app.controllers;

import app.model.entities.Figurita;
import app.model.entities.MedioComunicacion;
import app.model.entities.MedioDeContacto;
import app.model.entities.Subasta;
import app.model.entities.Perfil;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioPerfiles;
import static org.mockito.Mockito.when;

import java.util.List;
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

    @Test
    void crearSubastaNoFalla() throws Exception {
        String json = """
        {
            "figurita_id": "ARG-10",
            "duracion": 10
        }
        """;

        mockMvc.perform(post("/subastas")
                .header("user_id", "u-1000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    void ofertarEnSubastaNoFalla() throws Exception {
        String json = """
        {
            "usuario_id": "1001",
            "figuritas_ofrecidas_id": ["ARG-11"]
        }
        """;

        mockMvc.perform(post("/subastas/3000/propuestas")
                .header("user_id", "u-1001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }
}