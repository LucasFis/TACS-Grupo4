package app.controllers;

import app.dto.EstadisticasDto;
import app.dto.SesionDto;
import app.dto.request.LoginRequest;
import app.servicios.ServicioEstadisticas;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSesion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ControladorSesion.class)
@AutoConfigureMockMvc(addFilters = false)
class ControladorSesionTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ServicioEstadisticas estadisticasService;

  @MockBean
  private ServicioSesion servicioSesion;

  @MockBean
  private ServicioJwt servicioJwt;

  @Test
  void obtenerEstadisticas_retorna200() throws Exception {
    EstadisticasDto estadisticasDto = mock(EstadisticasDto.class);
    SesionDto sesionDto = mock(SesionDto.class);

    when(servicioJwt.obtenerSesion("fake-token"))
        .thenReturn(sesionDto);

    when(estadisticasService.obtenerEstadisticas(sesionDto))
        .thenReturn(estadisticasDto);

    mockMvc.perform(
            get("/administrador/estadisticas")
                .cookie(new jakarta.servlet.http.Cookie("token", "fake-token"))
        )
        .andExpect(status().isOk());
  }

  @Test
  void login_creaCookie_yRetorna204() throws Exception {

    when(servicioSesion.login(any(LoginRequest.class)))
        .thenReturn("jwt-token");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "nombre": "Juan",
                      "contrasenia": "1234"
                    }
                """))
        .andExpect(status().isNoContent())
        .andExpect(header().exists("Set-Cookie"))
        .andExpect(header().string("Set-Cookie",
            org.hamcrest.Matchers.containsString("token=jwt-token")))
        .andExpect(header().string("Set-Cookie",
            org.hamcrest.Matchers.containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie",
            org.hamcrest.Matchers.containsString("SameSite=Lax")));

    verify(servicioSesion).login(any(LoginRequest.class));
  }

  @Test
  void buscarUsuario_conCookie_devuelveSesionDto() throws Exception {

    SesionDto dto = new SesionDto(
        "user-1",
        "ADMIN",
        "perfil-1",
        "col-1"
    );

    when(servicioJwt.obtenerSesion("jwt-token"))
        .thenReturn(dto);

    mockMvc.perform(get("/yo")
            .cookie(new jakarta.servlet.http.Cookie("token", "jwt-token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario_id").value("user-1"))
        .andExpect(jsonPath("$.rol").value("ADMIN"))
        .andExpect(jsonPath("$.perfil_id").value("perfil-1"))
        .andExpect(jsonPath("$.col_id").value("col-1"));

    verify(servicioJwt).obtenerSesion("jwt-token");
  }

  @Test
  void cerrarSesion_borraCookie_yRetorna204() throws Exception {

    mockMvc.perform(delete("/sesion"))
        .andExpect(status().isNoContent())
        .andExpect(header().exists("Set-Cookie"))
        .andExpect(header().string("Set-Cookie",
            org.hamcrest.Matchers.containsString("token=")))
        .andExpect(header().string("Set-Cookie",
            org.hamcrest.Matchers.containsString("Max-Age=0")));
  }
}
