package app.controllers;

import app.servicios.ISubastaService;
import app.dto.SubastaDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subastas")
public class SubastaController {
    private final ISubastaService subastaService;

    SubastaController(ISubastaService subastaService) {
        this.subastaService = subastaService;
    }

    @PostMapping
    public ResponseEntity<SubastaDto> crearSubasta(@RequestHeader("user_id") String id, @RequestBody Map<String,Object> body) {
        String figuritaId = (String) body.get("figurita_id");
        LocalDateTime fechaInicio =  LocalDateTime.now();
        Number duracion = (Number) body.get("duracion");
        LocalDateTime fechaFin = fechaInicio.plusMinutes(duracion.longValue());

        SubastaDto subastaDto = this.subastaService.crearSubasta(id, fechaInicio, fechaFin, figuritaId, null);

        return ResponseEntity.ok(subastaDto);
    }

    @PostMapping("/{sub_id}/propuestas")
    public ResponseEntity<SubastaDto> ofertarEnSubasta(@PathVariable String sub_id, @RequestHeader("user_id") String id, @RequestBody Map<String,Object> body) {
        String usuarioDestino = (String) body.get("usuario_id");
        List<Object> rawFiguritasId = (ArrayList<Object>) body.get("figuritas_ofrecidas");

        SubastaDto subastaDto = this.subastaService.ofertarEnSubasta(id, usuarioDestino, sub_id, rawFiguritasId);

        return ResponseEntity.ok().body(subastaDto);
    }
}
