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
class PerfilControllerTest {

    @Autowired
    MockMvc mockMvc;
//TODO la logica va a cambiar, revisar test
//    @Test
//    void getOperaciones_usuarioExistente_retorna200ConDatos() throws Exception {
//        mockMvc.perform(get("/perfil/1000/operaciones"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.figuritas_publicadas").isArray())
//            .andExpect(jsonPath("$.propuestas_enviadas").isArray())
//            .andExpect(jsonPath("$.propuestas_recibidas").isArray())
//            .andExpect(jsonPath("$.subastas_activas").isArray());
//    }
//
//    @Test
//    void getOperaciones_usuarioInexistente_retorna404() throws Exception {
//        mockMvc.perform(get("/perfil/u-99/operaciones"))
//            .andExpect(status().isNotFound());
//    }

    @Test
    void calificarUsuarioNoFalla() throws Exception {
        mockMvc.perform(post("/perfil/1000/calificaciones")
                .header("autor_id", "1001")
                .contentType("application/json")
                .content("{ \"valor\": 4, \"descripcion\": \"Buen intercambio\" }"))
            .andExpect(status().isOk());
    }


//    @Test
//    void getNotificaciones() throws Exception {
//        mockMvc.perform(get("/usuarios/u-99/notificaciones")).andExpect(status().isOk());
//    }

    @Test
    void getIntercambiables_usuarioExistente_retorna200() throws Exception {
        mockMvc.perform(get("/perfil/1000/intercambiables"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getIntercambiables_usuarioInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/perfil/u-99/intercambiables"))
            .andExpect(status().isNotFound());
    }
}
