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

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {

    String path = request.getServletPath();

    if (request.getMethod().equals("OPTIONS")) return true;

    return path.startsWith("/login")
        || path.startsWith("/usuarios")
        || path.startsWith("/figuritas")
        || path.startsWith("/ping")
        || path.startsWith("/sesion");
  }

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
