package app.controllers;

import app.dto.SugerenciaDto;
import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSugerencia;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ControladorSugerenciasTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  ServicioSugerencia servicioSugerencia;

  @MockBean
  ServicioJwt servicioJwt;

  private final Cookie cookie = new Cookie("token", "fake-token");
  private final String perfilId = "1000";
  private final String sugerenciaId = "sugerencia-001";

  @BeforeEach
  void setup() {
    when(servicioJwt.getPerfilId("fake-token")).thenReturn(perfilId);
  }


  @Test
  void obtenerSugerencias_retorna200ConPagina() throws Exception {
    PaginaResultado<SugerenciaDto> pagina = new PaginaResultado<>(List.of(), 0L, 0, 0);

    when(servicioSugerencia.obtenerSugerencias(eq(perfilId), any(SugerenciasFiltro.class)))
        .thenReturn(pagina);

    mockMvc.perform(get("/sugerencias").cookie(cookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contenido").isArray());
  }

  @Test
  void obtenerSugerencias_conPaginacion_retorna200() throws Exception {
    PaginaResultado<SugerenciaDto> pagina = new PaginaResultado<>(List.of(), 0L, 0, 2);

    when(servicioSugerencia.obtenerSugerencias(eq(perfilId), any(SugerenciasFiltro.class)))
        .thenReturn(pagina);

    mockMvc.perform(get("/sugerencias")
            .cookie(cookie)
            .param("pagina", "2")
            .param("limite", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.numero").value(2));
  }

  @Test
  void obtenerSugerencias_perfilNoEncontrado_retorna404() throws Exception {
    when(servicioSugerencia.obtenerSugerencias(eq(perfilId), any(SugerenciasFiltro.class)))
        .thenThrow(new NotFoundException("Perfil no encontrado"));

    mockMvc.perform(get("/sugerencias").cookie(cookie))
        .andExpect(status().isNotFound());
  }

  @Test
  void alternarFavorito_retorna204() throws Exception {
    doNothing().when(servicioSugerencia).alternarFavorito(sugerenciaId, perfilId);

    mockMvc.perform(patch("/sugerencias/{id}/favorito", sugerenciaId).cookie(cookie))
        .andExpect(status().isNoContent());
  }

  @Test
  void alternarFavorito_sugerenciaNoEncontrada_retorna404() throws Exception {
    doThrow(new NotFoundException("Sugerencia no encontrada"))
        .when(servicioSugerencia).alternarFavorito(sugerenciaId, perfilId);

    mockMvc.perform(patch("/sugerencias/{id}/favorito", sugerenciaId).cookie(cookie))
        .andExpect(status().isNotFound());
  }

  @Test
  void alternarFavorito_sinCookie_retorna400() throws Exception {
    mockMvc.perform(patch("/sugerencias/{id}/favorito", sugerenciaId))
        .andExpect(status().isBadRequest());
  }
}