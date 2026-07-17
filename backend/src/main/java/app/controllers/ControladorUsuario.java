package app.controllers;

import app.dto.SesionDto;
import app.dto.request.ContraseniaRequest;
import app.dto.request.UsuarioRequest;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.servicios.ServicioJwt;
import app.servicios.ServicioUsuario;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ControladorUsuario {

  private final ServicioUsuario servicioUsuario;
  private final ServicioJwt servicioJwt;

  /**
   * Registra un nuevo usuario con rol estándar. Crea la cuenta, una colección vacía
   * y un perfil asociado. Además emite la cookie de sesión para que el cliente
   * quede autenticado de inmediato tras el registro.
   *
   * @param request  datos del usuario a registrar (nombre de usuario y contraseña)
   * @param response respuesta HTTP sobre la que se setea la cookie de sesión
   * @return 200 OK con los datos de sesión del usuario recién creado
   */
  @PostMapping("/usuarios")
  public ResponseEntity<SesionDto> registrarUsuario(
      @Valid @RequestBody UsuarioRequest request,
      HttpServletResponse response
  ) {
    Perfil perfil = this.servicioUsuario.registrarUsuario(request);

    String token = this.servicioJwt.generarToken(perfil.getUsuario(), perfil);

    ResponseCookie cookie = ResponseCookie.from("token", token)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.ofHours(12))
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    SesionDto sesion = this.servicioJwt.obtenerSesion(token);
    return ResponseEntity.ok(sesion);
  }

  /**
   * Cambia la contraseña del usuario autenticado. Valida la contraseña actual
   * antes de actualizarla.
   *
   * @param token token JWT del que se extrae el identificador del perfil
   * @param body  datos con la contraseña actual y la nueva contraseña
   * @return 200 OK si la contraseña se actualizó correctamente
   */
  @PutMapping("/usuarios/contrasenia")
  public ResponseEntity<Void> editarContrasenia(
      @CookieValue("token") String token,
      @Valid @RequestBody ContraseniaRequest body
  ) {
    String perfilId = this.servicioJwt.getPerfilId(token);
    this.servicioUsuario.editarContrasenia(perfilId, body.getContraseniaActual(), body.getContraseniaNueva());
    return ResponseEntity.ok().build();
  }

  /**
   * Registra un nuevo administrador. Solo un administrador existente puede
   * realizar esta operación.
   *
   * @param token   token JWT del que se extrae el rol del usuario autenticado
   * @param request datos del administrador a registrar
   * @return 204 No Content si el registro se realizó correctamente
   */
  @PostMapping("/administradores")
  public ResponseEntity<Void> registrarAdministrador(
      @CookieValue("token") String token,
      @Valid @RequestBody UsuarioRequest request
  ) {
    this.servicioUsuario.registrarAdministrador(request, Rol.valueOf(this.servicioJwt.getRol(token)));
    return ResponseEntity.noContent().build();
  }

  /**
   * Verifica si un nombre de usuario está disponible.
   *
   * @param nombre nombre de usuario a verificar
   * @return 200 OK si el nombre existe, 404 Not Found si está disponible
   */
  @RequestMapping(value = "/usuarios/{nombre}", method = RequestMethod.HEAD)
  public ResponseEntity<Void> verificarNombre(@PathVariable String nombre) {
    boolean existe = this.servicioUsuario.existeNombre(nombre);
    return existe ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
  }
}
