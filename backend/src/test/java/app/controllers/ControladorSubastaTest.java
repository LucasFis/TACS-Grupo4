package app.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.dto.request.EditarOfertaRequest;
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
    void buscarSubastasSinPaginacion_retorna200() throws Exception {
        mockMvc.perform(
                get("/subastas")
                .cookie(cookie)
                .contentType("application/json")
            )
            .andExpect(status().isOk());
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
                patch("/subastas/s-1/ofertas/o-1/seleccionar")
                    .cookie(cookie)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .seleccionarOferta("1000", "s-1", "o-1");
    }

    @Test
    void rechazarOferta_retorna200() throws Exception {
        mockMvc.perform(
                patch("/subastas/s-1/ofertas/o-1/rechazar")
                    .cookie(cookie)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .rechazarOferta("1000", "s-1", "o-1");
    }

    @Test
    void cancelarSubasta_retorna200() throws Exception {
        mockMvc.perform(
                patch("/subastas/s-1/cancelar")
                    .cookie(cookie)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .cancelarSubasta("1000", "s-1");
    }

    @Test
    void cerrarSubasta_retorna200() throws Exception {
        mockMvc.perform(
                patch("/subastas/s-1/cerrar")
                    .cookie(cookie)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .cerrarSubasta("1000", "s-1");
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

    @Test
    void crearSubastaFalla_figuritaIdNull() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":null,
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_figuritaIdVacio() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"",
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_figuritaIdEspacios() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"   ",
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_duracionEnHorasNull() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":null,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_duracionEnHorasCero() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":0,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_duracionEnHorasNegativa() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":-5,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_figuritasDeseadasIdsNull() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":null,
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_figuritasDeseadasIdsVacio() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":[],
                    "calificacion_minima":2
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_calificacionMinimaNull() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":null
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_calificacionMinimaNegativa() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":-1
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearSubastaFalla_calificacionMinimaMayorACinco() throws Exception {

        mockMvc.perform(
                post("/subastas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figurita_id":"ARG-10",
                    "duracion_en_horas":30,
                    "figuritas_deseadas_ids":["ARG-1"],
                    "calificacion_minima":6
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void ofertarEnSubastaNoFalla() throws Exception {

        mockMvc.perform(
                post("/subastas/sub-1/ofertas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figuritas_ofrecidas_id":[
                        "ARG-1",
                        "ARG-2"
                    ]
                }
                """)
            )
            .andExpect(status().isOk());

        verify(subastaService)
            .ofertarEnSubasta(
                eq("1000"),
                eq("sub-1"),
                eq(List.of("ARG-1", "ARG-2"))
            );
    }

    @Test
    void ofertarEnSubastaFalla_figuritasOfrecidasIdNull() throws Exception {

        mockMvc.perform(
                post("/subastas/sub-1/ofertas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figuritas_ofrecidas_id":null
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void ofertarEnSubastaFalla_figuritasOfrecidasIdVacio() throws Exception {

        mockMvc.perform(
                post("/subastas/sub-1/ofertas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figuritas_ofrecidas_id":[]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void editarOfertaEnSubastaFalla_figuritasOfrecidasIdNull() throws Exception {

        mockMvc.perform(
                patch("/subastas/sub-1/ofertas/oferta-1")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figuritas_ofrecidas_id":null
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void editarOfertaEnSubastaFalla_figuritasOfrecidasIdVacio() throws Exception {

        mockMvc.perform(
                patch("/subastas/sub-1/ofertas/oferta-1")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "figuritas_ofrecidas_id":[]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }
}