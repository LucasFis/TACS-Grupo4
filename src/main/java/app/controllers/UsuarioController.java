package app.controllers;

import app.dto.FiguritaIntercambiableDto;
import app.dto.NotificacionesDto;
import app.dto.OperacionesDto;
import app.dto.SugerenciaDto;
import app.dto.TemporalDto;
import app.model.entities.Sugerencia;
import app.model.notificador.Notificacion;
import app.servicios.IUsuarioService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{user_id}/operaciones")
    public ResponseEntity<OperacionesDto> obtenerOperaciones(@PathVariable String user_id) {
        OperacionesDto operaciones = usuarioService.obtenerOperacionesUsuario(user_id);
        if (operaciones == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(operaciones);
    }

    @PostMapping("/{user_id}/calificaciones")
    public ResponseEntity<TemporalDto> calificarUsuario(@PathVariable String user_id, @RequestBody Map<String, Object> body) {
        Number calificacionMedia = this.usuarioService.agregarCalificacion((Integer) body.get("calificacion"), user_id);

        return ResponseEntity.ok(new TemporalDto("Nueva calificacion: " + calificacionMedia));
    }

    @GetMapping("/{user_id}/intercambiables")
    public ResponseEntity<List<FiguritaIntercambiableDto>> obtenerIntercambiables(
        @PathVariable String user_id) {

        return ResponseEntity.ok(
            usuarioService.obtenerIntercambiablesUsuario(user_id)
        );
    }
    @GetMapping("/{user_id}/sugerencias")
    public ResponseEntity<List<SugerenciaDto>> obtenerSugerencias(@PathVariable String user_id) {
        List<SugerenciaDto> sugerenciasDto = this.usuarioService.obtenerSugerencias(user_id);

        return ResponseEntity.accepted().body(sugerenciasDto);
    }
    @GetMapping("/{user_id}/notificaciones")
    public ResponseEntity<List<NotificacionesDto>> obtenerNotificaciones(@PathVariable String user_id) {
        return ResponseEntity.ok(this.usuarioService.obtenerNotificaciones(user_id));
    }
}