package app.controllers;

import app.model.entities.*;
import app.servicios.impl.PropuestaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PropuestaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PropuestaService propuestaService;

    private Usuario origen;
    private Usuario destino;

    @BeforeEach
    void setUp() {
        origen = new Usuario("1", "Origen", null, "", List.of());
        destino = new Usuario("2", "Destino", null, "", List.of());

        Propuesta propuesta = new Propuesta(
                "1",
                origen,
                destino,
                List.of(),
                null,
                EstadoProceso.PENDIENTE
        );

        propuestaService.crear(propuesta);
    }

    @Test
    void crearPropuestaNoFalla() throws Exception {
        mockMvc.perform(post("/propuestas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoint").value("POST /propuestas"));
    }

    @Test
    void aceptarPropuestaNoFalla() throws Exception {
        mockMvc.perform(patch("/propuestas/1/aceptar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoint").value("Propuesta 1 aceptada"));
    }

    @Test
    void rechazarPropuestaNoFalla() throws Exception {
        mockMvc.perform(patch("/propuestas/1/rechazar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoint").value("Propuesta 1 rechazada"));
    }
}