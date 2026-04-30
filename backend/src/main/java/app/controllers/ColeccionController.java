package app.controllers;

import app.dto.request.FaltanteRequest;
import app.dto.request.RepetidaRequest;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.servicios.IColeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colecciones")
@RequiredArgsConstructor
public class ColeccionController {

    private final IColeccionService coleccionService;

    @PostMapping("/{col_id}/faltantes")
    public ResponseEntity<Void> agregarFaltante(
        @PathVariable String col_id,
        @RequestBody FaltanteRequest request) {

        coleccionService.agregarFaltante(col_id, request.getFigId());

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/{col_id}/repetidas")
    public ResponseEntity<Void> agregarRepetida(
        @PathVariable String col_id,
        @RequestHeader("user_id") String userId,
        @RequestBody RepetidaRequest request) {

        coleccionService.agregarRepetida(col_id, userId,
            request.getFigId(), request.getCantidadExistente(), request.getModosIntercambio());

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{col_id}/faltantes")
    public ResponseEntity<List<Figurita>> buscarFaltantes(
        @PathVariable String col_id) {

        List<Figurita> faltantes = coleccionService.buscarFaltantes(col_id);

        return ResponseEntity.ok(faltantes);
    }

    @GetMapping("/{col_id}/repetidas")
    public ResponseEntity<List<FiguritaIntercambiable>> buscarRepetidas(
        @PathVariable String col_id,
        @RequestParam(defaultValue = "false") boolean subasta,
        @RequestParam(defaultValue = "false") boolean intercambio,
        @RequestParam(defaultValue = "false") boolean ambos
        ) {

        List<FiguritaIntercambiable> faltantes = coleccionService.buscarRepetidas(col_id, subasta, intercambio, ambos);

        return ResponseEntity.ok(faltantes);
    }

}
