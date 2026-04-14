package app.controllers;

import app.dto.TemporalDto;
import app.servicios.impl.PropuestaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/propuestas")
public class PropuestaController {

    private final PropuestaService propuestaService;

    public PropuestaController(PropuestaService propuestaService) {
        this.propuestaService = propuestaService;
    }

    @PostMapping
    public ResponseEntity<TemporalDto> crearPropuesta() {
        return ResponseEntity.ok(new TemporalDto("POST /propuestas"));
    }

    @PatchMapping("/{prop_id}/aceptar")
    public ResponseEntity<TemporalDto> aceptar(@PathVariable String prop_id) {
        propuestaService.aceptar(prop_id);
        return ResponseEntity.ok(new TemporalDto("Propuesta " + prop_id + " aceptada"));
    }

    @PatchMapping("/{prop_id}/rechazar")
    public ResponseEntity<TemporalDto> rechazar(@PathVariable String prop_id) {
        propuestaService.rechazar(prop_id);
        return ResponseEntity.ok(new TemporalDto("Propuesta " + prop_id + " rechazada"));
    }
}
