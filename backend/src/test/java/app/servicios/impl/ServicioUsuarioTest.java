package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.MongoTestBase;
import app.exceptions.BadRequestException;
import app.model.entities.Coleccion;
import app.model.entities.Perfil;
import app.model.entities.Rol;
import app.model.entities.Usuario;
import app.servicios.ServicioUsuario;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ServicioUsuarioTest extends MongoTestBase {

  private ServicioUsuario service;
  private Perfil perfil;

  @BeforeEach
  void setUp() {
    service = new ServicioUsuario(repositorioUsuarios, repositorioPerfiles, repositorioColecciones, new SimpleMeterRegistry());

    String contraseniaEncriptada = new BCryptPasswordEncoder().encode("contrasenia123");
    Usuario user = new Usuario("u-1", Rol.USUARIO, "lucas", contraseniaEncriptada);
    Coleccion colec = new Coleccion("c-1");
    perfil = Perfil.builder()
        .id("p-1").usuario(user).nombre("Lucas")
        .coleccion(colec)
        .build();
    repositorioColecciones.guardar(colec);
    repositorioUsuarios.guardar(user);
    repositorioPerfiles.guardar(perfil);
  }

  @Test
  void editarContrasenia_contraseniaActualCorrecta_actualizaExitosamente() {
    assertDoesNotThrow(() ->
        service.editarContrasenia("p-1", "contrasenia123", "nuevaContrasenia456")
    );
  }

  @Test
  void editarContrasenia_contraseniaActualIncorrecta_lanzaExcepcion() {
    assertThrows(BadRequestException.class, () ->
        service.editarContrasenia("p-1", "contraseniaErronea", "nuevaContrasenia456")
    );
  }
}
