package app.controllers;

import app.dto.CalificacionDto;
import app.dto.ContadorDto;
import app.dto.NotificacionDto;
import app.dto.PerfilDto;
import app.dto.SugerenciaDto;
import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CalificacionRequest;
import app.dto.request.PerfilRequest;
import app.servicios.ServicioJwt;
import app.servicios.ServicioPerfil;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class ControladorPerfil {

    private final ServicioPerfil perfilService;
    private final ServicioJwt servicioJwt;

    /**
     * Califica a otro perfil como parte de una transacción (intercambio o subasta).
     *
     * @param token token JWT del que se extrae el identificador del perfil calificador
     * @param body  datos de la calificación (destinatario, valor, descripción, transacción)
     * @return 200 OK si la calificación se registró correctamente
     */
    @PostMapping("/calificaciones")
    public ResponseEntity<Void> calificarPerfil(
        @CookieValue("token") String token,
        @Valid @RequestBody CalificacionRequest body
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.perfilService.agregarCalificacion(
            perfilId,
            body.getDestinatarioId(),
            body.getValor(),
            body.getDescripcion(),
            body.getTransactionId(),
            body.getTipoTransaccion()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene las calificaciones recibidas por el perfil autenticado, de forma paginada.
     *
     * @param token  token JWT del que se extrae el identificador del perfil
     * @param pagina número de página solicitado
     * @param limite cantidad máxima de resultados por página
     * @return 200 OK con la página de calificaciones recibidas
     */
    @GetMapping("/calificaciones")
    public ResponseEntity<PaginaResultado<CalificacionDto>> obtenerCalificaciones(
        @CookieValue("token") String token,
        @RequestParam Integer pagina,
        @RequestParam Integer limite
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.ok(this.perfilService.obtenerCalificaciones(perfilId, pagina, limite));
    }

    /**
     * Obtiene los contadores de figuritas repetidas y faltantes del perfil autenticado.
     *
     * @param token token JWT del que se extrae el identificador del perfil
     * @return 200 OK con la lista de contadores (repetidas, faltantes)
     */
    @GetMapping("/contadores")
    public ResponseEntity<List<ContadorDto>> obtenerContadores(
        @CookieValue("token") String token
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.ok(this.perfilService.obtenerContadores(perfilId));
    }

    /**
     * Obtiene las notificaciones del perfil autenticado.
     *
     * @param token token JWT del que se extrae el identificador del perfil
     * @return 200 OK con la lista de notificaciones del perfil
     */
    @GetMapping("/notificaciones")
    public ResponseEntity<List<NotificacionDto>> obtenerNotificaciones(
        @CookieValue String token
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.ok(this.perfilService.obtenerNotificaciones(perfilId));
    }

    /**
     * Marca todas las notificaciones del perfil autenticado como leídas.
     *
     * @param token token JWT del que se extrae el identificador del perfil
     * @return 204 No Content si la operación se realizó correctamente
     */
    @PatchMapping("/notificaciones/leidas")
    public ResponseEntity<Void> marcarTodasLeidas(
            @CookieValue String token
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.perfilService.marcarTodasNotifsLeidas(perfilId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene los datos completos del perfil autenticado, incluyendo medios de contacto.
     *
     * @param token token JWT del que se extrae el identificador del perfil
     * @return 200 OK con los datos del perfil
     */
    @GetMapping
    public ResponseEntity<PerfilDto> obtenerPerfil(
        @CookieValue("token") String token
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.ok(this.perfilService.obtenerPerfil(perfilId));
    }

    /**
     * Edita los datos del perfil autenticado: nombre, nombre de usuario y/o medios de contacto.
     *
     * @param token token JWT del que se extrae el identificador del perfil
     * @param body  datos actualizados del perfil
     * @return 200 OK si la edición se realizó correctamente
     */
    @PutMapping
    public ResponseEntity<Void> editarPerfil(
        @CookieValue("token") String token,
        @Valid @RequestBody PerfilRequest body
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.perfilService.editarPerfil(perfilId, body);
        return ResponseEntity.ok().build();
    }

}