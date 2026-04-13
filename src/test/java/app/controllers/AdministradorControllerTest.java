package app.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdministradorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getEstadisticas_retorna200ConDatos() throws Exception {
        mockMvc.perform(get("/administrador/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_usuarios").isNumber())
                .andExpect(jsonPath("$.total_figuritas_publicadas").isNumber())
                .andExpect(jsonPath("$.total_propuestas").isNumber())
                .andExpect(jsonPath("$.total_subastas_activas").isNumber());
    }

}
