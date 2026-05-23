package app.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.dto.subasta.SubastaDto;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSubasta;
import jakarta.servlet.http.Cookie;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ControladorSubastaTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ServicioSubasta subastaService;

    @MockBean
    ServicioJwt servicioJwt;

    private final Cookie cookie =
        new Cookie("token","fake-token");

    @BeforeEach
    void setup() {
        when(servicioJwt.getPerfilId("fake-token"))
            .thenReturn("1000");
    }

    @Test
    void crearSubasta_retorna200() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                    {
                        "figurita_id":"ARG-10",
                        "duracion_en_horas":30,
                        "figuritas_deseadas_ids":["ARG-1"],
                        "calificacion_minima":2
                    }
                    """)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .crearSubasta(
                eq("1000"),
                eq("ARG-10"),
                eq(30),
                eq(List.of("ARG-1")),
                eq(2)
            );
    }

    @Test
    void ofertarEnSubasta_retorna200() throws Exception {

        mockMvc.perform(
                post("/subastas/s-1/ofertas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                    {
                      "figuritas_ofrecidas_id":[
                        "ARG-11"
                      ]
                    }
                    """)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .ofertarEnSubasta(
                "1000",
                "s-1",
                List.of("ARG-11")
            );
    }

    @Test
    void editarOferta_retorna200() throws Exception {

        mockMvc.perform(
                patch("/subastas/s-1/ofertas/o-1")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                  "figuritas_ofrecidas_id":[
                    "ARG-15"
                  ]
                }
                """)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .editarOfertaEnSubasta(
                eq("1000"),
                eq("s-1"),
                eq("o-1"),
                any()
            );
    }

    @Test
    void cancelarOferta_retorna200() throws Exception {

        mockMvc.perform(
                patch("/subastas/s-1/ofertas/o-1/cancelar")
                    .cookie(cookie)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .cancelarOferta(
                "1000",
                "s-1",
                "o-1"
            );
    }

    @Test
    void seleccionarOferta_retorna200() throws Exception {

        mockMvc.perform(
                post("/subastas/s-1/ofertas/o-1/seleccionar")
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .seleccionarOferta(
                "s-1",
                "o-1"
            );
    }

    @Test
    void rechazarOferta_retorna200() throws Exception {

        mockMvc.perform(
                post("/subastas/s-1/ofertas/o-1/rechazar")
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .rechazarOferta(
                "s-1",
                "o-1"
            );
    }

    @Test
    void cancelarSubasta_retorna200() throws Exception {

        mockMvc.perform(
                post("/subastas/s-1/cancelar")
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .cancelarSubasta("s-1");
    }

    @Test
    void cerrarSubasta_retorna200() throws Exception {

        mockMvc.perform(
                post("/subastas/s-1/cerrar")
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .cerrarSubasta("s-1");
    }



    @Test
    void obtenerSubasta_retorna200() throws Exception {

        SubastaDto dto = mock(SubastaDto.class);

        when(
            subastaService.obtenerSubasta("s-1")
        ).thenReturn(dto);

        mockMvc.perform(
                get("/subastas/s-1")
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .obtenerSubasta("s-1");
    }
}