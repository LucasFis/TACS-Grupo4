package app.controllers;

import app.dto.FiguritaDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.filtros.FaltantesFiltro;
import app.dto.filtros.RepetidasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.paginacion.Repetidas;
import app.dto.request.EditarRepetidaRequest;
import app.dto.request.FaltanteRequest;
import app.dto.request.RepetidaRequest;
import app.servicios.ServicioColeccion;
import app.servicios.ServicioJwt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/colecciones")
@RequiredArgsConstructor
public class ControladorColeccion {

    private final ServicioColeccion coleccionService;
    private final ServicioJwt servicioJwt;

    /**
     * Agrega una figurita a la lista de faltantes de la colección del usuario autenticado.
     *
     * @param token   token JWT del que se extrae el identificador de la colección
     * @param request datos de la figurita a marcar como faltante
     * @return 201 Created si la operación se realizó correctamente
     */
    @PostMapping("/faltantes")
    public ResponseEntity<Void> agregarFaltante(
        @CookieValue("token") String token,
        @Valid @RequestBody FaltanteRequest request
    ) {
        String colId = this.servicioJwt.getColeccionId(token);
        coleccionService.agregarFaltante(colId, request.getFigId());

        return ResponseEntity.status(201).build();
    }

    /**
     * Agrega una figurita repetida a la colección del usuario autenticado, marcándola
     * como disponible para intercambio. Notifica a los perfiles que la tienen como faltante.
     *
     * @param token   token JWT del que se extraen el identificador de la colección y del perfil
     * @param request datos de la figurita repetida (identificador, cantidad, modos de intercambio)
     * @return 201 Created si la operación se realizó correctamente
     */
    @PostMapping("/repetidas")
    public ResponseEntity<Void> agregarRepetida(
        @CookieValue("token") String token,
        @Valid @RequestBody RepetidaRequest request
    ) {
        String colId = this.servicioJwt.getColeccionId(token);
        String perfilId = this.servicioJwt.getPerfilId(token);
        coleccionService.agregarRepetida(colId, perfilId,
            request.figId(), request.cantidadExistente(), request.modosIntercambio());

        return ResponseEntity.status(201).build();
    }

    /**
     * Busca las figuritas faltantes de la colección del usuario autenticado,
     * aplicando los filtros de búsqueda y paginación proporcionados.
     *
     * @param token   token JWT del que se extrae el identificador de la colección
     * @param filtros criterios de filtrado y paginación
     * @return 200 OK con la página de figuritas faltantes encontradas
     */
    @GetMapping("/faltantes")
    public ResponseEntity<PaginaResultado<FiguritaDto>> buscarFaltantes(
        @CookieValue("token") String token,
        @ModelAttribute FaltantesFiltro filtros
    ) {
        String colId = this.servicioJwt.getColeccionId(token);
        return ResponseEntity.ok(this.coleccionService.buscarFaltantes(colId, filtros));
    }

    /**
     * Busca las figuritas repetidas de la colección del usuario autenticado,
     * aplicando los filtros de búsqueda y paginación proporcionados.
     *
     * @param token   token JWT del que se extrae el identificador de la colección
     * @param filtros criterios de filtrado y paginación
     * @return 200 OK con las figuritas repetidas encontradas, incluyendo contadores
     *         de publicadas y disponibles
     */
    @GetMapping("/repetidas")
    public ResponseEntity<Repetidas<FiguritaIntercambiableDto>> buscarRepetidas(
        @CookieValue("token") String token,
        @ModelAttribute RepetidasFiltro filtros
    ) {
        String colId = this.servicioJwt.getColeccionId(token);
        return ResponseEntity.ok(this.coleccionService.buscarRepetidas(colId, filtros));
    }

    @PatchMapping("/repetidas/{fig_id}")
    public ResponseEntity<Void> editarRepetida(
        @CookieValue("token") String token,
        @PathVariable String fig_id,
        @Valid @RequestBody EditarRepetidaRequest req
    ) {
        String colId = this.servicioJwt.getColeccionId(token);
        this.coleccionService.editarRepetida(colId, fig_id, req);
        return ResponseEntity.status(204).build();
    }
}
