package app.controllers;

import app.dto.FiguritaIntercambiableDto;
import app.model.entities.Seleccion;
import app.servicios.IFiguritaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor

public class FiguritaController {
    private final IFiguritaService figuritaService;

    @GetMapping("/figuritas")
    public ResponseEntity<List<FiguritaIntercambiableDto>> obtenerFiguritas(
        @RequestParam(required = false) Integer numero,
        @RequestParam(required = false) Seleccion seleccion,
        @RequestParam(required = false) String jugador
    ) {
        return ResponseEntity.ok(figuritaService.buscarFiguritas(numero, seleccion, jugador));
    }
}
