package app.middlewares;

import app.exceptions.UnauthorizedException;
import app.servicios.ServicioJwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FiltroJwtTest {

  private ServicioJwt servicioJwt;
  private HandlerExceptionResolver errorHandler;
  private CorsConfigurationSource corsConfigurationSource;

  private JwtFilter jwtFilter;

  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    servicioJwt = mock(ServicioJwt.class);
    errorHandler = mock(HandlerExceptionResolver.class);
    corsConfigurationSource = mock(CorsConfigurationSource.class);

    jwtFilter = new JwtFilter(servicioJwt, errorHandler, corsConfigurationSource);

    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);
  }

  // -----------------------------
  // shouldNotFilter
  // -----------------------------

  @Test
  void shouldNotFilter_rutasPublicas_devuelveTrue() {
    when(request.getServletPath()).thenReturn("/login/test");
    when(request.getMethod()).thenReturn("GET");

    assertTrue(jwtFilter.shouldNotFilter(request));
  }

  @Test
  void shouldNotFilter_rutasProtegidas_devuelveFalse() {
    when(request.getServletPath()).thenReturn("/subastas/123");
    when(request.getMethod()).thenReturn("GET");

    assertFalse(jwtFilter.shouldNotFilter(request));
  }

  @Test
  void shouldNotFilter_options_devuelveTrue() {
    when(request.getMethod()).thenReturn("OPTIONS");
    when(request.getServletPath()).thenReturn("/subastas/123");

    assertTrue(jwtFilter.shouldNotFilter(request));
  }

  // -----------------------------
  // doFilterInternal - éxito
  // -----------------------------

  @Test
  void doFilter_tokenValido_permiteContinuarFiltro() throws Exception {
    Cookie cookie = new Cookie("token", "token-valido");

    when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    when(servicioJwt.validarToken("token-valido")).thenReturn(null);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(servicioJwt).validarToken("token-valido");
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(errorHandler);
  }

  // -----------------------------
  // doFilterInternal - errores de token
  // -----------------------------

  @Test
  void doFilter_sinCookies_lanzaUnauthorized() throws Exception {
    when(request.getCookies()).thenReturn(null);

    when(corsConfigurationSource.getCorsConfiguration(request))
        .thenReturn(null);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(errorHandler).resolveException(
        eq(request),
        eq(response),
        isNull(),
        any(UnauthorizedException.class)
    );
  }

  @Test
  void doFilter_cookieSinToken_lanzaUnauthorized() throws Exception {
    Cookie cookie = new Cookie("otra", "valor");

    when(request.getCookies()).thenReturn(new Cookie[]{cookie});

    when(corsConfigurationSource.getCorsConfiguration(request))
        .thenReturn(null);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(errorHandler).resolveException(
        eq(request),
        eq(response),
        isNull(),
        any(UnauthorizedException.class)
    );
  }

  @Test
  void doFilter_tokenInvalido_jwtException_manejaCorsYError() throws Exception {
    Cookie cookie = new Cookie("token", "bad-token");

    when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    when(request.getHeader("Origin")).thenReturn("http://localhost:3000");

    CorsConfiguration corsConfig = mock(CorsConfiguration.class);

    when(corsConfigurationSource.getCorsConfiguration(request))
        .thenReturn(corsConfig);

    when(corsConfig.checkOrigin("http://localhost:3000"))
        .thenReturn("allowed");

    when(servicioJwt.validarToken("bad-token"))
        .thenThrow(new JwtException("invalid"));

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(response).setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
    verify(response).setHeader("Access-Control-Allow-Credentials", "true");

    verify(errorHandler).resolveException(
        eq(request),
        eq(response),
        isNull(),
        any(UnauthorizedException.class)
    );
  }

  @Test
  void doFilter_tokenInvalido_sinCors_noSeteaHeaders() throws Exception {
    Cookie cookie = new Cookie("token", "bad-token");

    when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    when(servicioJwt.validarToken("bad-token"))
        .thenThrow(new JwtException("invalid"));

    when(corsConfigurationSource.getCorsConfiguration(request))
        .thenReturn(null);

    jwtFilter.doFilterInternal(request, response, filterChain);

    verify(response, never()).setHeader(anyString(), anyString());

    verify(errorHandler).resolveException(
        eq(request),
        eq(response),
        isNull(),
        any(UnauthorizedException.class)
    );
  }
}
