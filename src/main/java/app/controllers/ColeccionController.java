package app.controllers;

import app.dto.request.FaltanteRequest;
import app.dto.request.RepetidaRequest;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.servicios.ColeccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/colecciones")
public class ColeccionController {

    private final ColeccionService coleccionService;

    public ColeccionController(ColeccionService coleccionService) {
        this.coleccionService = coleccionService;
    }

    @PostMapping("/{col_id}/faltantes")
    public ResponseEntity<Figurita> agregarFaltante(
        @PathVariable String col_id,
        @RequestBody FaltanteRequest request) {

        coleccionService.agregarFaltante(col_id, request.getFigId());

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/{col_id}/repetidas")
    public ResponseEntity<FiguritaIntercambiable> agregarRepetida(
        @PathVariable String col_id,
        @RequestHeader("userId") String userId,
        @RequestBody RepetidaRequest request) {

        coleccionService.agregarRepetida(col_id, userId,
            request.getFigId(), request.getCantidadDisponible(), request.getModosIntercambio());

        return ResponseEntity.status(201).build();
    }

}
