package app.servicios;

import app.dto.request.LoginRequest;
import app.exceptions.UsuarioException;
import app.model.entities.Perfil;
import app.model.entities.Usuario;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioUsuarios;
import app.repositories.impl.campos.CamposPerfil;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioSesion {
  private final RepositorioUsuarios repoUsuario;
  private final RepositorioPerfiles repoPerfiles;
  private final ServicioJwt servicioJwt;
  private final MeterRegistry meterRegistry;

  /**
   * Autentica un usuario con sus credenciales y genera un token JWT válido
   * que incluye los datos de sesión (usuarioId, rol, perfilId, colId).
   *
   * @param request credenciales de inicio de sesión (nombre de usuario y contraseña)
   * @return token JWT firmado con los datos de sesión del usuario
   * @throws app.exceptions.UsuarioException si las credenciales son inválidas
   * @throws app.exceptions.NotFoundException si no se encuentra el usuario o el perfil
   */
  public String login(LoginRequest request) {
    try {
      String token = intentarLogin(request);
      meterRegistry.counter("auth_login_intentos_total", "resultado", "exitoso").increment();
      return token;
    } catch (RuntimeException e) {
      meterRegistry.counter("auth_login_intentos_total", "resultado", "fallido").increment();
      throw e;
    }
  }

  private String intentarLogin(LoginRequest request) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    Usuario usuario = this.repoUsuario.buscarPorNombre(request.nombre());

    boolean coincide = passwordEncoder.matches(request.contrasenia(), usuario.getContrasenia());

    if (!coincide) {
      throw new UsuarioException("Credenciales invalidas");
    }

    CamposPerfil sinCampos = new CamposPerfil(false);

    Perfil perfil = this.repoPerfiles.buscarPorUsuarioId(usuario.getId(), sinCampos);

    return this.servicioJwt.generarToken(usuario, perfil);
  }
}
