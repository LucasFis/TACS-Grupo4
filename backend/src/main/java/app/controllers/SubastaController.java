package app.controllers;

import app.dto.request.CrearSubastaRequest;
import app.dto.request.OfertarEnSubastaRequest;
import app.servicios.ISubastaService;
import app.dto.SubastaDto;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SubastaController {
    private final ISubastaService subastaService;

    @PostMapping
    public ResponseEntity<SubastaDto> crearSubasta(@RequestHeader("user_id") String id, @RequestBody CrearSubastaRequest body) {
        LocalDateTime fechaInicio =  LocalDateTime.now();
        LocalDateTime fechaFin = fechaInicio.plusMinutes(body.getDuracion().longValue());

        SubastaDto subastaDto = this.subastaService.crearSubasta(id, fechaInicio, fechaFin, body.getFiguritaId());

        return ResponseEntity.ok(subastaDto);
    }

    @PostMapping("/{sub_id}/propuestas")
    public ResponseEntity<SubastaDto> ofertarEnSubasta(@PathVariable String sub_id, @RequestHeader("user_id") String id, @RequestBody OfertarEnSubastaRequest body) {

        SubastaDto subastaDto = this.subastaService.ofertarEnSubasta(id, body.getUsuarioId(), sub_id, body.getFiguritasOfrecidasId());

        return ResponseEntity.ok().body(subastaDto);
    }
}
