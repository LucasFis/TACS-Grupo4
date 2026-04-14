package app.controllers;

import app.dto.OperacionesDto;
import app.dto.TemporalDto;
import app.model.entities.Sugerencia;
import app.model.notificador.Notificacion;
import app.servicios.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{user_id}/operaciones")
    public ResponseEntity<OperacionesDto> getOperaciones(@PathVariable String user_id) {
        OperacionesDto operaciones = usuarioService.getOperacionesUsuario(user_id);
        if (operaciones == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(operaciones);
    }

    @PostMapping("/{user_id}/calificaciones")
    public ResponseEntity<TemporalDto> calificarUsuario(@PathVariable String user_id, @RequestBody Map<String, Object> body) {
        try {
            Number calificacionMedia = this.usuarioService.agregarCalificacion((Integer) body.get("calificacion"), user_id);
            return ResponseEntity.ok(new TemporalDto("Nueva calificacion: " + calificacionMedia));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new TemporalDto("Bad request: " + e.getMessage()));
        }
    }

    @GetMapping("/{user_id}/sugerencias")
    public ResponseEntity<?> getSugerencias(@PathVariable String user_id) {
        try {
            List<Sugerencia> sugerencias = this.usuarioService.getSugerencias(user_id);

            return ResponseEntity.ok(sugerencias); //TODO: Falta DTO, envio mucha informacion del usuario sugerido
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new TemporalDto("Bad request: " + e.getMessage()));
        }
    }
    @GetMapping("/{user_id}/notificaciones")
    public ResponseEntity<List<Notificacion>> getNotificaciones(@PathVariable String user_id) {
        return ResponseEntity.ok(this.usuarioService.getNotificaciones(user_id));
    }
}