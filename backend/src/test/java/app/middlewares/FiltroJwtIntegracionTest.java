package app.middlewares;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.servicios.ServicioJwt;
import app.servicios.ServicioPerfil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FiltroJwtIntegracionTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  ServicioJwt servicioJwt;

  @MockBean
  ServicioPerfil perfilService;

  @Test
  void endpointProtegido_sinCookie_retorna401() throws Exception {
    mockMvc.perform(get("/perfil"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void endpointProtegido_cookieSinToken_retorna401() throws Exception {
    mockMvc.perform(get("/perfil")
            .cookie(new Cookie("otra-cookie", "valor")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void endpointProtegido_tokenInvalido_retorna401() throws Exception {
    when(servicioJwt.validarToken("token-malo"))
        .thenThrow(new JwtException("Token inválido"));

    mockMvc.perform(get("/perfil")
            .cookie(new Cookie("token", "token-malo")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void endpointProtegido_tokenValido_retorna200() throws Exception {
    when(servicioJwt.validarToken("token-bueno")).thenReturn(null);
    when(servicioJwt.getPerfilId("token-bueno")).thenReturn("p-1");
    when(perfilService.obtenerPerfil("p-1")).thenReturn(null);

    mockMvc.perform(get("/perfil")
            .cookie(new Cookie("token", "token-bueno")))
        .andExpect(status().isOk());
  }

}
