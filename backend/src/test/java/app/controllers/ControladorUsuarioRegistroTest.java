package app.controllers;

import app.MongoTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración del auto-login post-registro.
 * Verifica que POST /usuarios setee la cookie de sesión y que los atributos
 * sean idénticos a los del login (HttpOnly, SameSite, Path, Secure).
 */
@AutoConfigureMockMvc(addFilters = false)
class ControladorUsuarioRegistroTest extends MongoTestBase {

  @Autowired
  private MockMvc mockMvc;

  private static final String JSON_USUARIO_VALIDO = """
      {
          "nombre": "usuarioNuevo123",
          "contrasenia": "Contrasenia1!",
          "rol": "USUARIO"
      }
      """;

  @Test
  void registroExitoso_devuelve200ConCookieToken() throws Exception {
    mockMvc.perform(post("/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_USUARIO_VALIDO))
        .andExpect(status().isOk())
        .andExpect(header().exists("Set-Cookie"))
        .andExpect(header().string("Set-Cookie", containsString("token=")))
        .andExpect(header().string("Set-Cookie", not(containsString("token=;"))));
  }

  @Test
  void registroExitoso_cookieTieneAtributosIgualesALogin() throws Exception {
    mockMvc.perform(post("/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_USUARIO_VALIDO))
        .andExpect(status().isOk())
        .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
        .andExpect(header().string("Set-Cookie", containsString("Path=/")))
        .andExpect(header().string("Set-Cookie", containsString("Secure")));
  }

  @Test
  void registroExitoso_devuelveSesionDtoConPerfilId() throws Exception {
    mockMvc.perform(post("/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_USUARIO_VALIDO))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario_id").isNotEmpty())
        .andExpect(jsonPath("$.rol").value("USUARIO"))
        .andExpect(jsonPath("$.perfil_id").isNotEmpty())
        .andExpect(jsonPath("$.col_id").isNotEmpty());
  }

  @Test
  void registroExitoso_cookiePermiteAccesoAEndpointAutenticado() throws Exception {
    // Registrar y obtener la cookie de sesión
    MvcResult resultado = mockMvc.perform(post("/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_USUARIO_VALIDO))
        .andExpect(status().isOk())
        .andReturn();

    String setCookieHeader = resultado.getResponse().getHeader("Set-Cookie");
    // Extraer solo el valor "token=<jwt>" del header Set-Cookie
    String cookieToken = setCookieHeader.split(";")[0]; // "token=<jwt>"
    String tokenValue = cookieToken.substring("token=".length());

    // Con la cookie recibida, un GET /yo debe responder 200
    mockMvc.perform(get("/yo")
            .cookie(new jakarta.servlet.http.Cookie("token", tokenValue)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rol").value("USUARIO"));
  }

  @Test
  void registroConNombreDuplicado_devuelve400SinCookie() throws Exception {
    // Primer registro: OK
    mockMvc.perform(post("/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_USUARIO_VALIDO))
        .andExpect(status().isOk());

    // Segundo registro con el mismo nombre: debe fallar
    mockMvc.perform(post("/usuarios")
            .contentType(MediaType.APPLICATION_JSON)
            .content(JSON_USUARIO_VALIDO))
        .andExpect(status().isBadRequest())
        .andExpect(header().doesNotExist("Set-Cookie"));
  }
}
