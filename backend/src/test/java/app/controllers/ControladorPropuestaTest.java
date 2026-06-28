package app.controllers;

import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearPropuestaRequest;
import app.exceptions.ForbiddenException;
import app.exceptions.UnauthorizedException;
import app.servicios.ServicioJwt;
import app.servicios.ServicioPropuesta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControladorPropuestaTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ServicioPropuesta propuestaService;

    @MockBean
    ServicioJwt servicioJwt;

    private final Cookie cookie =
        new Cookie("token", "fake-token");

    @BeforeEach
    void setup() {
        when(servicioJwt.getPerfilId("fake-token"))
            .thenReturn("1000");
    }

    @Test
    void buscarPropuestasSinPaginacion_retorna200() throws Exception {
        mockMvc.perform(
                get("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json"))
            .andExpect(status().isOk());
    }

    @Test
    void crearPropuesta_retorna201() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                    {
                        "destinatario_id":"2000",
                        "figurita_buscada_id":"ARG10",
                        "figuritas_ofrecidas_ids":[
                            "ARG1",
                            "ARG2"
                        ]
                    }
                    """)
            )
            .andExpect(status().isCreated());

        verify(propuestaService)
            .crearPropuesta(
                eq("1000"),
                any(CrearPropuestaRequest.class)
            );
    }

    @Test
    void crearPropuesta_enviaRequestCorrectamente() throws Exception {

        mockMvc.perform(
            post("/propuestas")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                    {
                        "destinatario_id":"2000",
                        "figurita_buscada_id":"ARG10",
                        "figuritas_ofrecidas_ids":[
                            "ARG1",
                            "ARG2"
                        ]
                    }
                    """)
        );

        ArgumentCaptor<CrearPropuestaRequest> captor =
            ArgumentCaptor.forClass(
                CrearPropuestaRequest.class
            );

        verify(propuestaService)
            .crearPropuesta(
                eq("1000"),
                captor.capture()
            );

        CrearPropuestaRequest request =
            captor.getValue();

        assertEquals(
            "2000",
            request.getDestinatarioId()
        );

        assertEquals(
            "ARG10",
            request.getFiguritaBuscadaId()
        );

        assertEquals(
            2,
            request.getFiguritasOfrecidasIds()
                .size()
        );
    }

    @Test
    void aceptarPropuesta_retorna204() throws Exception {

        mockMvc.perform(
                patch("/propuestas/p-1/aceptar")
                    .cookie(cookie)
            )
            .andExpect(status().isNoContent());

        verify(propuestaService)
            .aceptar("p-1","1000");
    }

    @Test
    void rechazarPropuesta_retorna204() throws Exception {

        mockMvc.perform(
                patch("/propuestas/p-1/rechazar")
                    .cookie(cookie)
            )
            .andExpect(status().isNoContent());

        verify(propuestaService)
            .rechazar("p-1","1000");
    }

    @Test
    void cancelarPropuesta_devuelve403_siNoEsElDuenio()
        throws Exception {

        doThrow(new ForbiddenException(
            "No sos el dueño de la propuesta"
        ))
            .when(propuestaService)
            .cancelar("p-1", "1000");

        mockMvc.perform(
                patch("/propuestas/p-1/cancelar")
                    .cookie(cookie)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void cancelarPropuesta_retorna204() throws Exception {

        mockMvc.perform(
                patch("/propuestas/p-1/cancelar")
                    .cookie(cookie)
            )
            .andExpect(status().isNoContent());

        verify(propuestaService)
            .cancelar("p-1","1000");
    }

    @Test
    void obtenerPropuestas_retorna200() throws Exception {

        when(propuestaService.buscarPropuestas(
            eq("1000"),
            any()
        )).thenReturn(
            new PaginaResultado<>(
                List.of(),
                0,
                0,
                0
            )
        );

        mockMvc.perform(
                get("/propuestas")
                    .cookie(cookie)
            )
            .andExpect(status().isOk());

        verify(propuestaService)
            .buscarPropuestas(
                eq("1000"),
                any()
            );
    }

    @Test
    void obtenerPropuestas_enviaFiltrosCorrectamente()
        throws Exception {

        when(propuestaService.buscarPropuestas(
            eq("1000"),
            any()
        )).thenReturn(
            new PaginaResultado<>(
                List.of(),
                0,
                0,
                0
            )
        );

        mockMvc.perform(
            get("/propuestas")
                .cookie(cookie)
                .param("tipo", "RECIBIDAS")
                .param("pagina", "1")
                .param("limite", "10")
                .param("estado", "PENDIENTE")
        );

        ArgumentCaptor<PropuestasFiltro> captor =
            ArgumentCaptor.forClass(
                PropuestasFiltro.class
            );

        verify(propuestaService)
            .buscarPropuestas(
                eq("1000"),
                captor.capture()
            );

        PropuestasFiltro filtros =
            captor.getValue();

        assertEquals(
            "RECIBIDAS",
            filtros.tipo()
        );

        assertEquals(
            1,
            filtros.pagina()
        );

        assertEquals(
            10,
            filtros.limite()
        );
    }

    @Test
    void endpoints_usanTokenParaObtenerPerfil()
        throws Exception {

        mockMvc.perform(
            patch("/propuestas/p-1/aceptar")
                .cookie(cookie)
        );

        verify(servicioJwt)
            .getPerfilId("fake-token");
    }
    @Test
    void crearPropuestaFalla_destinatarioIdNull() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":null,
                    "figurita_buscada_id":"ARG10",
                    "figuritas_ofrecidas_ids":[
                        "ARG1",
                        "ARG2"
                    ]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearPropuestaFalla_destinatarioIdVacio() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":"",
                    "figurita_buscada_id":"ARG10",
                    "figuritas_ofrecidas_ids":[
                        "ARG1",
                        "ARG2"
                    ]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearPropuestaFalla_destinatarioIdEspacios() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":"   ",
                    "figurita_buscada_id":"ARG10",
                    "figuritas_ofrecidas_ids":[
                        "ARG1",
                        "ARG2"
                    ]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearPropuestaFalla_figuritaBuscadaIdNull() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":"2000",
                    "figurita_buscada_id":null,
                    "figuritas_ofrecidas_ids":[
                        "ARG1",
                        "ARG2"
                    ]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearPropuestaFalla_figuritaBuscadaIdVacio() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":"2000",
                    "figurita_buscada_id":"",
                    "figuritas_ofrecidas_ids":[
                        "ARG1",
                        "ARG2"
                    ]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearPropuestaFalla_figuritaBuscadaIdEspacios() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":"2000",
                    "figurita_buscada_id":"   ",
                    "figuritas_ofrecidas_ids":[
                        "ARG1",
                        "ARG2"
                    ]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearPropuestaFalla_figuritasOfrecidasIdsNull() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":"2000",
                    "figurita_buscada_id":"ARG10",
                    "figuritas_ofrecidas_ids":null
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearPropuestaFalla_figuritasOfrecidasIdsVacio() throws Exception {

        mockMvc.perform(
                post("/propuestas")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""
                {
                    "destinatario_id":"2000",
                    "figurita_buscada_id":"ARG10",
                    "figuritas_ofrecidas_ids":[]
                }
                """)
            )
            .andExpect(status().isBadRequest());
    }
}