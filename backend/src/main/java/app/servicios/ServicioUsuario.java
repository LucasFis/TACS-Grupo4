package app.servicios;

import app.dto.request.UsuarioRequest;
import app.exceptions.BadRequestException;
import app.exceptions.ForbiddenException;
import app.model.entities.Coleccion;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Usuario;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioUsuarios;
import app.repositories.impl.campos.CamposPerfil;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ServicioUsuario {

  private final RepositorioUsuarios repositorioUsuarios;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioColecciones repositorioColecciones;
  private final MeterRegistry meterRegistry;

  /**
   * Registra un nuevo usuario con rol {@link Rol#USUARIO}. Crea la cuenta de usuario,
   * una colección vacía y un perfil asociado.
   *
   * @param request datos del usuario a registrar (nombre, contraseña)
   * @return el perfil recién creado
   * @throws app.exceptions.BadRequestException si el nombre de usuario ya está en uso
   */
  public Perfil registrarUsuario(UsuarioRequest request) {
    request.setRol(Rol.USUARIO);

    return this.registrar(request);
  }

    /**
     * Registra un nuevo administrador. Solo puede ser invocada por otro administrador.
     *
     * @param request datos del usuario administrador a registrar
     * @param rol     rol que debe ser {@link Rol#ADMINISTRADOR} para autorizar el registro
     * @throws app.exceptions.ForbiddenException si el rol proporcionado no es ADMINISTRADOR
     * @throws app.exceptions.BadRequestException   si el nombre de usuario ya está en uso
     */
    public void registrarAdministrador(UsuarioRequest request, Rol rol) {

        if(rol == Rol.ADMINISTRADOR) {
            this.registrar(request);
        } else {
            throw new ForbiddenException("Acceso denegado por rol invalido");
        }
    }


  /**
   * Cambia la contraseña de un usuario. Valida que la contraseña actual sea correcta
   * antes de actualizarla.
   *
   * @param perfilId          identificador del perfil cuyo usuario cambiará la contraseña
   * @param contraseniaActual contraseña actual del usuario
   * @param contraseniaNueva  nueva contraseña a establecer
   * @throws app.exceptions.BadRequestException si la contraseña actual es incorrecta
   * @throws app.exceptions.NotFoundException  si no se encuentra el perfil indicado
   */
  public void editarContrasenia(String perfilId, String contraseniaActual, String contraseniaNueva) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    Perfil perfil = this.repositorioPerfiles.buscarPorId(perfilId, new CamposPerfil(false));
    Usuario usuario = perfil.getUsuario();
    if (!passwordEncoder.matches(contraseniaActual, usuario.getContrasenia())) {
      throw new BadRequestException("La contraseña actual es incorrecta");
    }
    this.repositorioUsuarios.guardar(
        new Usuario(usuario.getId(), usuario.getRol(), usuario.getNombre(), passwordEncoder.encode(contraseniaNueva))
    );
  }

  /**
   * Ejecuta la lógica común de registro: valida que el nombre no esté en uso,
   * crea el usuario, la colección vacía y el perfil asociado.
   *
   * @param request datos del usuario a registrar
   * @return el perfil recién creado
   * @throws app.exceptions.BadRequestException si el nombre de usuario ya está en uso
   */
  private Perfil registrar(UsuarioRequest request) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    Usuario usuarioNuevo;

    if (this.repositorioUsuarios.existePorNombre(request.getNombre())) {
      throw new BadRequestException("El nombre de usuario ya está en uso");
    }

    if(request.getRol() == null) {
      usuarioNuevo = new Usuario(request.getNombre(), passwordEncoder.encode(request.getContrasenia()), Rol.USUARIO);
    } else {
      usuarioNuevo = new Usuario(request.getNombre(), passwordEncoder.encode(request.getContrasenia()), request.getRol());
    }

    this.repositorioUsuarios.guardar(usuarioNuevo);
    meterRegistry.counter("usuarios_registrados_total", "rol", usuarioNuevo.getRol().name().toLowerCase()).increment();

    Coleccion coleccion = new Coleccion();

    this.repositorioColecciones.guardar(coleccion);

    Perfil perfil = Perfil.builder()
        .usuario(usuarioNuevo)
        .nombre(usuarioNuevo.getNombre())
        .coleccion(coleccion)
        .build();

    this.repositorioPerfiles.guardar(perfil);

    return perfil;
  }
  /**
   * Verifica si un nombre de usuario ya está registrado en el sistema.
   *
   * @param nombre nombre de usuario a verificar
   * @return {@code true} si el nombre ya existe, {@code false} en caso contrario
   */
  public boolean existeNombre(String nombre) {
    return this.repositorioUsuarios.existePorNombre(nombre);
  }
}


