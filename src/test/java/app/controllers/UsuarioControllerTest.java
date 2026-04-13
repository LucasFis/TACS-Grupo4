package app.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getOperaciones_usuarioExistente_retorna200ConDatos() throws Exception {
        mockMvc.perform(get("/usuarios/1000/operaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.figuritas_publicadas").isArray())
                .andExpect(jsonPath("$.propuestas_enviadas").isArray())
                .andExpect(jsonPath("$.propuestas_recibidas").isArray())
                .andExpect(jsonPath("$.subastas_activas").isArray());
    }

    @Test
    void getOperaciones_usuarioInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/usuarios/u-99/operaciones"))
                .andExpect(status().isNotFound());
    }

    @Test
    void calificarUsuarioNoFalla() throws Exception {
        mockMvc.perform(post("/usuarios/1/calificaciones")).andExpect(status().isOk());
    }
}
