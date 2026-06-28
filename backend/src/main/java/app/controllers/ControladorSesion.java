package app.controllers;

import app.dto.EstadisticasDto;
import app.dto.SesionDto;
import app.dto.request.LoginRequest;
import app.exceptions.BadRequestException;
import app.servicios.ServicioEstadisticas;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSesion;
import jakarta.servlet.http.HttpServletResponse;
import app.dto.request.FiltroFechasRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequiredArgsConstructor
public class ControladorSesion {

    private final ServicioEstadisticas estadisticasService;
    private final ServicioSesion servicioSesion;
    private final ServicioJwt servicioJwt;


    /**
     * Obtiene las estadísticas generales del sistema (usuarios, figuritas publicadas,
     * propuestas, subastas activas, etc.). Solo accesible para administradores.
     *
     * @param token token JWT del que se extraen los datos de sesión para validar el rol
     * @return 200 OK con las estadísticas del sistema
     */
    @GetMapping("/administrador/estadisticas")
    public ResponseEntity<EstadisticasDto> obtenerEstadisticas(
        @CookieValue("token") String token,
        @ModelAttribute @jakarta.validation.Valid FiltroFechasRequest filtro
    ) {
        LocalDate fechaDesde;
        LocalDate fechaHasta;
        try {
            fechaDesde = LocalDate.parse(filtro.desde());
            fechaHasta = LocalDate.parse(filtro.hasta());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Formato de fecha inválido. Use YYYY-MM-DD");
        }

        if (fechaDesde.isAfter(fechaHasta)) {
            throw new BadRequestException("'desde' no puede ser posterior a 'hasta'");
        }

        SesionDto dto = this.servicioJwt.obtenerSesion(token);
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticas(dto, fechaDesde, fechaHasta));
    }

    /**
     * Inicia sesión con las credenciales proporcionadas. Si son válidas,
     * establece una cookie httpOnly con el token JWT.
     *
     * @param request  credenciales de inicio de sesión (nombre y contraseña)
     * @param response respuesta HTTP sobre la que se setea la cookie de sesión
     * @return 204 No Content con la cookie de sesión establecida
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(
        @RequestBody LoginRequest request,
        HttpServletResponse response
    ) {
        String token = this.servicioSesion.login(request);

        ResponseCookie cookie =
            ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofHours(12))
                .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            cookie.toString()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene los datos de sesión del usuario autenticado (usuarioId, rol, perfilId, colId).
     *
     * @param token token JWT del que se extraen los datos de sesión
     * @return 200 OK con los datos de sesión del usuario
     */
    @GetMapping("/yo")
    public ResponseEntity<SesionDto> buscarUsuario(
        @CookieValue("token") String token
    ) {
        SesionDto dto = this.servicioJwt.obtenerSesion(token);
        return ResponseEntity.ok(dto);
    }

    /**
     * Cierra la sesión del usuario eliminando la cookie de autenticación.
     *
     * @param response respuesta HTTP sobre la que se elimina la cookie de sesión
     * @return 204 No Content con la cookie expirada
     */
    @DeleteMapping("/sesion")
    public ResponseEntity<Void> cerrarSesion(
        HttpServletResponse response
    ) {

        ResponseCookie cookie =
            ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            cookie.toString()
        );

        return ResponseEntity.noContent().build();
    }
}
