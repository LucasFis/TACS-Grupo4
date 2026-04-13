package app.controllers;

import app.dto.OperacionesDto;
import app.dto.TemporalDto;
import app.servicios.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<TemporalDto> calificarUsuario(@PathVariable String user_id) {
        return ResponseEntity.ok(new TemporalDto("POST /usuarios/" + user_id + "/calificaciones"));
    }

}
