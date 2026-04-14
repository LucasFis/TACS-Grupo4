package app.controllers;

import app.dto.TemporalDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/propuestas")
public class PropuestaController {

    @PostMapping
    public ResponseEntity<TemporalDto> crearPropuesta() {
        return ResponseEntity.ok(new TemporalDto("POST /propuestas"));
    }

    @PatchMapping("/{prop_id}")
    public ResponseEntity<TemporalDto> responderPropuesta(@PathVariable String prop_id) {
        return ResponseEntity.ok(new TemporalDto("PATCH /propuestas/" + prop_id));
    }
}
