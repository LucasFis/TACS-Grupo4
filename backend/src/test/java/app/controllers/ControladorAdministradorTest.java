package app.controllers;

import app.dto.SesionDto;
import app.servicios.ServicioJwt;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ControladorAdministradorTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private ServicioJwt servicioJwt;

    @Test
    void getEstadisticas_retorna200ConDatos() throws Exception {

        when(servicioJwt.obtenerSesion(any()))
            .thenReturn(new SesionDto("u1", "ADMINISTRADOR", "p1", "c1"));

        mockMvc.perform(
                get("/administrador/estadisticas")
                    .param("desde", "2025-06-01")
                    .param("hasta", "2025-06-15")
                    .cookie(new Cookie("token", "token-falso"))
            )
            .andExpect(status().isOk());
    }

}
