package app.servicios.impl;

import app.dto.request.LoginRequest;
import app.exceptions.UsuarioException;
import app.model.entities.Perfil;
import app.model.entities.Usuario;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioUsuarios;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSesion;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioSesionTest {

  private RepositorioUsuarios repoUsuario;
  private RepositorioPerfiles repoPerfiles;
  private ServicioJwt servicioJwt;

  private ServicioSesion servicioSesion;

  @BeforeEach
  void setUp() {
    repoUsuario = mock(RepositorioUsuarios.class);
    repoPerfiles = mock(RepositorioPerfiles.class);
    servicioJwt = mock(ServicioJwt.class);

    servicioSesion = new ServicioSesion(
        repoUsuario,
        repoPerfiles,
        servicioJwt,
        new SimpleMeterRegistry()
    );
  }

  @Test
  void login_correcto_devuelveToken() {

    LoginRequest request = mock(LoginRequest.class);
    when(request.nombre()).thenReturn("juan");
    when(request.contrasenia()).thenReturn("1234");

    Usuario usuario = mock(Usuario.class);
    when(usuario.getId()).thenReturn("user-1");
    when(usuario.getContrasenia()).thenReturn(
        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            .encode("1234")
    );

    Perfil perfil = mock(Perfil.class);

    when(repoUsuario.buscarPorNombre("juan")).thenReturn(usuario);
    when(repoPerfiles.buscarPorUsuarioId(eq("user-1"), any()))
        .thenReturn(perfil);
    when(servicioJwt.generarToken(usuario, perfil)).thenReturn("jwt-token");

    String token = servicioSesion.login(request);

    assertEquals("jwt-token", token);

    verify(repoUsuario).buscarPorNombre("juan");
    verify(repoPerfiles).buscarPorUsuarioId(eq("user-1"), any());
    verify(servicioJwt).generarToken(usuario, perfil);
  }

  @Test
  void login_passwordIncorrecta_lanzaExcepcion() {

    LoginRequest request = mock(LoginRequest.class);
    when(request.nombre()).thenReturn("juan");
    when(request.contrasenia()).thenReturn("wrong");

    Usuario usuario = mock(Usuario.class);
    when(usuario.getContrasenia()).thenReturn(
        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            .encode("1234")
    );

    when(repoUsuario.buscarPorNombre("juan")).thenReturn(usuario);

    assertThrows(UsuarioException.class, () -> {
      servicioSesion.login(request);
    });

    verify(repoPerfiles, never()).buscarPorUsuarioId(any(), any());
    verify(servicioJwt, never()).generarToken(any(), any());
  }

  @Test
  void login_usuarioNoExiste_lanzaNullPointerOrExcepcion() {

    LoginRequest request = mock(LoginRequest.class);
    when(request.nombre()).thenReturn("juan");

    when(repoUsuario.buscarPorNombre("juan")).thenReturn(null);

    assertThrows(Exception.class, () -> {
      servicioSesion.login(request);
    });
  }
}
