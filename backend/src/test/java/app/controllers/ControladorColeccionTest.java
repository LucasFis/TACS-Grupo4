package app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.dto.request.EditarRepetidaRequest;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.MetodoIntercambio;
import app.servicios.ServicioColeccion;
import app.servicios.ServicioJwt;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControladorColeccionTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  private ServicioColeccion serviceColeccion;

  @MockBean
  private ServicioJwt servicioJwt;

  private final Cookie cookie = new Cookie("token", "fake-token");

  @BeforeEach
  void setup() {
    when(servicioJwt.getColeccionId("fake-token"))
        .thenReturn("1");
  }

  @Test
  void agregarRepetidaNoFalla() throws Exception {
    String json = """
            {
                "fig_id": "ARG-10",
                "cantidad_existente": 2,
                "modos_intercambio": ["SUBASTA"]
            }
            """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated());
  }

  @Test
  void agregarFaltanteNoFalla() throws Exception {
    String json = """
            {
                "fig_id": "ARG-10"
            }
            """;

    mockMvc.perform(post("/colecciones/faltantes")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated());
  }

  @Test
  void agregarFaltanteNoEncontradaDevuelve404() throws Exception {

    doThrow(new NotFoundException("No se encontro la figurita"))
        .when(serviceColeccion)
        .agregarFaltante("1", "ARG-10");

    String json = """
            {
                "fig_id": "ARG-10"
            }
            """;

    mockMvc.perform(post("/colecciones/faltantes")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  void agregarRepetidaNoEncontradaDevuelve404() throws Exception {

    doThrow(new NotFoundException("No se encontro la figurita"))
        .when(serviceColeccion)
        .agregarRepetida(eq("1"), any(),any(), any(), any());

    String json = """
            {
                "fig_id": "ARG-10",
                "cantidad_existente": 2,
                "modos_intercambio": ["SUBASTA"]
            }
            """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  void agregarFaltanteDevuelve400SiDuplicada() throws Exception {

    doThrow(new BadRequestException("Figurita ya listada como faltante"))
        .when(serviceColeccion)
        .agregarFaltante(eq("1"), any());

    String json = """
            {
                "fig_id": "ARG-10"
            }
            """;

    mockMvc.perform(post("/colecciones/faltantes")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  //Con bodys invalidos
  @Test
  void agregarRepetidaFalla_figIdNull() throws Exception {
    String json = """
      {
          "fig_id": null,
          "cantidad_existente": 2,
          "modos_intercambio": ["SUBASTA"]
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarRepetidaFalla_figIdVacio() throws Exception {
    String json = """
      {
          "fig_id": "",
          "cantidad_existente": 2,
          "modos_intercambio": ["SUBASTA"]
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarRepetidaFalla_figIdEspacios() throws Exception {
    String json = """
      {
          "fig_id": "   ",
          "cantidad_existente": 2,
          "modos_intercambio": ["SUBASTA"]
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarRepetidaFalla_cantidadExistenteNull() throws Exception {
    String json = """
      {
          "fig_id": "ARG-10",
          "cantidad_existente": null,
          "modos_intercambio": ["SUBASTA"]
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarRepetidaFalla_cantidadExistenteCero() throws Exception {
    String json = """
      {
          "fig_id": "ARG-10",
          "cantidad_existente": 0,
          "modos_intercambio": ["SUBASTA"]
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarRepetidaFalla_cantidadExistenteNegativa() throws Exception {
    String json = """
      {
          "fig_id": "ARG-10",
          "cantidad_existente": -1,
          "modos_intercambio": ["SUBASTA"]
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarRepetidaFalla_modosIntercambioNull() throws Exception {
    String json = """
      {
          "fig_id": "ARG-10",
          "cantidad_existente": 2,
          "modos_intercambio": null
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarRepetidaFalla_modosIntercambioVacio() throws Exception {
    String json = """
      {
          "fig_id": "ARG-10",
          "cantidad_existente": 2,
          "modos_intercambio": []
      }
      """;

    mockMvc.perform(post("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarFaltanteFalla_fig_idNull() throws Exception {
    String json = """
            {
                "fig_id": null
            }
            """;

    mockMvc.perform(post("/colecciones/faltantes")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }
  @Test
  void agregarFaltanteFalla_fig_idVacio() throws Exception {
    String json = """
            {
                "fig_id": ""
            }
            """;

    mockMvc.perform(post("/colecciones/faltantes")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void editarRepetida_retorna204() throws Exception {
    mockMvc.perform(
            patch("/colecciones/repetidas/{fig_id}", "ARG-10")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "cantidad_nueva": 5,
                      "metodos": ["INTERCAMBIO", "SUBASTA"]
                    }
                """)
        )
        .andExpect(status().isNoContent());

    verify(servicioJwt).getColeccionId("fake-token");

    verify(serviceColeccion).editarRepetida(
        eq("1"),
        eq("ARG-10"),
        any(EditarRepetidaRequest.class)
    );
  }

  @Test
  void editarRepetida_enviaRequestCorrectoAlService() throws Exception {
    mockMvc.perform(
            patch("/colecciones/repetidas/{fig_id}", "ARG-10")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "cantidad_nueva": 5,
                      "metodos": ["INTERCAMBIO", "SUBASTA"]
                    }
                """)
        )
        .andExpect(status().isNoContent());

    ArgumentCaptor<EditarRepetidaRequest> captor =
        ArgumentCaptor.forClass(EditarRepetidaRequest.class);

    verify(serviceColeccion).editarRepetida(
        eq("1"),
        eq("ARG-10"),
        captor.capture()
    );

    EditarRepetidaRequest req = captor.getValue();

    assertEquals(5, req.cantidadNueva());
    assertEquals(
        List.of(
            MetodoIntercambio.INTERCAMBIO,
            MetodoIntercambio.SUBASTA
        ),
        req.metodos()
    );
  }

  @Test
  void editarRepetida_retorna400SiServiceFalla() throws Exception {
    doThrow(new BadRequestException("error"))
        .when(serviceColeccion)
        .editarRepetida(
            anyString(),
            anyString(),
            any(EditarRepetidaRequest.class)
        );

    mockMvc.perform(
            patch("/colecciones/repetidas/{fig_id}", "ARG-10")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "cantidad_nueva": 5,
                      "metodos": ["INTERCAMBIO"]
                    }
                """)
        )
        .andExpect(status().isBadRequest());
  }
  @Test
  void buscarFaltantesSinPaginacionRetorna_200() throws Exception {
    mockMvc.perform(get("/colecciones/faltantes")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
  @Test
  void buscarRepetidasSinPaginacionRetorna_200() throws Exception {
    mockMvc.perform(get("/colecciones/repetidas")
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}