package app.middlewares;

import app.exceptions.UnauthorizedException;
import app.servicios.ServicioJwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que se ejecuta en cada petición HTTP.
 * Extrae el token de la cookie {@code token}, lo valida y, si es inválido,
 * devuelve un error 401 con los encabezados CORS adecuados.
 * <p>
 * Excluye de la validación las rutas públicas: {@code /login}, {@code /usuarios},
 * {@code /figuritas}, {@code /ping} y {@code /sesion}, así como las peticiones
 * {@code OPTIONS} (preflight CORS).
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

  private final ServicioJwt servicioJwt;
  private final HandlerExceptionResolver errorHandler;
  private final CorsConfigurationSource corsConfigurationSource;

  public JwtFilter(
      ServicioJwt servicioJwt,
      @Qualifier("handlerExceptionResolver")
      HandlerExceptionResolver errorHandler,
      CorsConfigurationSource corsConfigurationSource
  ){
    this.servicioJwt = servicioJwt;
    this.errorHandler = errorHandler;
    this.corsConfigurationSource = corsConfigurationSource;
  }

  /**
   * Determina si la petición actual debe omitir el filtro JWT.
   * Excluye rutas públicas (login, registro, figuritas, ping, sesión)
   * y peticiones OPTIONS (preflight CORS).
   *
   * @param request petición HTTP entrante
   * @return {@code true} si la ruta está excluida del filtro
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {

    String path = request.getServletPath();

    if(request.getMethod().equals("OPTIONS")) return true;

    return path.startsWith("/login")
        || path.startsWith("/usuarios")
        || path.startsWith("/figuritas")
        || path.startsWith("/ping")
        || path.startsWith("/sesion");
  }

  /**
   * Valida el token JWT de la cookie y, si es correcto, continúa con la cadena
   * de filtros. Si el token es inválido o no está presente, configura los
   * encabezados CORS para el origen solicitado y delega el error al
   * {@link HandlerExceptionResolver}.
   *
   * @param request     petición HTTP entrante
   * @param response    respuesta HTTP
   * @param filterChain cadena de filtros a continuar
   * @throws ServletException si ocurre un error en el filtrado
   * @throws IOException      si ocurre un error de E/S
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    try {

      servicioJwt.validarToken(
          obtenerToken(request)
      );

      filterChain.doFilter(
          request,
          response
      );

    } catch (JwtException | UnauthorizedException e) {

      var corsConfig =
          corsConfigurationSource.getCorsConfiguration(request);

      if (corsConfig != null) {

        String origin = request.getHeader("Origin");

        if (corsConfig.checkOrigin(origin) != null) {

          response.setHeader(
              "Access-Control-Allow-Origin",
              origin
          );

          response.setHeader(
              "Access-Control-Allow-Credentials",
              "true"
          );
        }
      }

      errorHandler.resolveException(
          request,
          response,
          null,
          new UnauthorizedException("Token inválido")
      );
    }
  }

  /**
   * Extrae el token JWT de las cookies de la petición.
   *
   * @param request petición HTTP entrante
   * @return valor del token JWT
   * @throws app.exceptions.UnauthorizedException si no se encuentra la cookie {@code token}
   */
  private String obtenerToken(HttpServletRequest request) {

    Cookie[] cookies = request.getCookies();

    if(cookies == null) {
      throw new UnauthorizedException(
          "No se encontró el token"
      );
    }

    for(Cookie cookie : cookies) {
      if("token".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }

    throw new UnauthorizedException(
        "No se encontró el token"
    );
  }

}
