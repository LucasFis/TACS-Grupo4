package app.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.dto.*;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Usuario;
import app.servicios.ServicioJwt;
import app.servicios.ServicioPerfil;
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
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControladorPerfilTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  ServicioPerfil perfilService;

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
  void obtenerPerfil_retorna200() throws Exception {

    Usuario usuario = new Usuario(
        "u-1",
        Rol.USUARIO,
        "juan",
        "1234"
    );

    Perfil perfil = Perfil.builder()
        .id("1000")
        .usuario(usuario)
        .nombre("Perfil 1")
        .calificacionMedia(0.0)
        .build();

    when(perfilService.obtenerPerfil("1000"))
        .thenReturn(new PerfilDto(perfil));

    mockMvc.perform(
            get("/perfil")
                .cookie(cookie)
        )
        .andExpect(status().isOk());

    verify(perfilService)
        .obtenerPerfil("1000");
  }

  @Test
  void obtenerNotificaciones_retorna200() throws Exception {

    when(perfilService.obtenerNotificaciones("1000"))
        .thenReturn(List.of());

    mockMvc.perform(
            get("/perfil/notificaciones")
                .cookie(cookie)
        )
        .andExpect(status().isOk());

    verify(perfilService)
        .obtenerNotificaciones("1000");
  }

  @Test
  void actualizarPerfilNoFalla() throws Exception {
    mockMvc.perform(
            put("/perfil")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "nombre": "Lucas",
                    "nombre_usuario": "lucas123",
                    "medios_de_contacto": []
                  }
                  """)
        )
        .andExpect(status().isOk());
  }

  @Test
  void obtenerContadores_retorna200() throws Exception {

    when(perfilService.obtenerContadores("1000"))
        .thenReturn(List.of());

    mockMvc.perform(
            get("/perfil/contadores")
                .cookie(cookie)
        )
        .andExpect(status().isOk());

    verify(perfilService)
        .obtenerContadores("1000");
  }

  @Test
  void obtenerCalificaciones_retorna200() throws Exception {

    when(perfilService.obtenerCalificaciones(
        "1000",
        0,
        10
    )).thenReturn(
        new PaginaResultado<>(List.of(),0,0,0)
    );

    mockMvc.perform(
            get("/perfil/calificaciones")
                .cookie(cookie)
                .param("pagina","0")
                .param("limite","10")
        )
        .andExpect(status().isOk());

    verify(perfilService)
        .obtenerCalificaciones(
            "1000",
            0,
            10
        );
  }

  @Test
  void calificarPerfil_noFalla() throws Exception {

    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                    {
                      "destinatario_id":"1001",
                      "valor":4,
                      "descripcion":"Buen intercambio",
                      "transaction_id":"i-1",
                      "tipo_transaccion":"INTERCAMBIO"
                    }
                    """)
        )
        .andExpect(status().isOk());

    verify(perfilService)
        .agregarCalificacion(
            eq("1000"),
            eq("1001"),
            eq(4),
            eq("Buen intercambio"),
            eq("i-1"),
            eq(MetodoIntercambio.INTERCAMBIO)
        );
  }

  @Test
  void agregarCalificacionFalla_valorNull() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"1001",
                    "valor":null,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"i-1",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_valorMenorAUno() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"1001",
                    "valor":0,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"i-1",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_valorMayorACinco() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"1001",
                    "valor":6,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"i-1",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_destinatarioIdNull() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":null,
                    "valor":4,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"i-1",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_destinatarioIdVacio() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"",
                    "valor":4,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"i-1",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_destinatarioIdEspacios() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"   ",
                    "valor":4,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"i-1",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_transactionIdNull() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"1001",
                    "valor":4,
                    "descripcion":"Buen intercambio",
                    "transaction_id":null,
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_transactionIdVacio() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"1001",
                    "valor":4,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_transactionIdEspacios() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"1001",
                    "valor":4,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"   ",
                    "tipo_transaccion":"INTERCAMBIO"
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void agregarCalificacionFalla_tipoTransaccionNull() throws Exception {
    mockMvc.perform(
            post("/perfil/calificaciones")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "destinatario_id":"1001",
                    "valor":4,
                    "descripcion":"Buen intercambio",
                    "transaction_id":"i-1",
                    "tipo_transaccion":null
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }
  @Test
  void actualizarPerfilFalla_nombreNull() throws Exception {
    mockMvc.perform(
            put("/perfil")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "nombre": null,
                    "nombre_usuario": "lucas123",
                    "medios_de_contacto": []
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void actualizarPerfilFalla_nombreVacio() throws Exception {
    mockMvc.perform(
            put("/perfil")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "nombre": "",
                    "nombre_usuario": "lucas123",
                    "medios_de_contacto": []
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void actualizarPerfilFalla_nombreEspacios() throws Exception {
    mockMvc.perform(
            put("/perfil")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "nombre": "   ",
                    "nombre_usuario": "lucas123",
                    "medios_de_contacto": []
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void actualizarPerfilFalla_nombreUsuarioNull() throws Exception {
    mockMvc.perform(
            put("/perfil")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "nombre": "Lucas",
                    "nombre_usuario": null,
                    "medios_de_contacto": []
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void actualizarPerfilFalla_nombreUsuarioVacio() throws Exception {
    mockMvc.perform(
            put("/perfil")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "nombre": "Lucas",
                    "nombre_usuario": "",
                    "medios_de_contacto": []
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void actualizarPerfilFalla_nombreUsuarioEspacios() throws Exception {
    mockMvc.perform(
            put("/perfil")
                .cookie(cookie)
                .contentType("application/json")
                .content("""
                  {
                    "nombre": "Lucas",
                    "nombre_usuario": "   ",
                    "medios_de_contacto": []
                  }
                  """)
        )
        .andExpect(status().isBadRequest());
  }
}