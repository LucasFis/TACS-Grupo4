package app.controllers;

import app.dto.IntercambioDto;
import app.dto.PropuestaDto;
import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearPropuestaRequest;
import app.servicios.ServicioJwt;
import app.servicios.ServicioPropuesta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/propuestas")
public class ControladorPropuesta {
    private final ServicioPropuesta propuestaService;
    private final ServicioJwt servicioJwt;

    /**
     * Crea una nueva propuesta de intercambio. Valida que la figurita buscada esté
     * en los faltantes del autor y que las figuritas ofrecidas existan.
     *
     * @param token   token JWT del que se extrae el identificador del perfil autor
     * @param request datos de la propuesta (destinatario, figurita buscada, figuritas ofrecidas)
     * @return 201 Created con los datos de la propuesta creada
     */
    @PostMapping
    public ResponseEntity<PropuestaDto> crearPropuesta(
        @CookieValue("token") String token,
        @Valid @RequestBody CrearPropuestaRequest request
    ) {
        String autorId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.status(201).body(propuestaService.crearPropuesta(autorId, request));
    }

    /**
     * Obtiene una propuesta de intercambio por su identificador.
     *
     * @param token  token JWT del que se extrae el identificador del perfil
     * @param prop_id identificador de la propuesta
     * @return 200 OK con los datos de la propuesta (clasificada como ENVIADA o RECIBIDA)
     */
    @GetMapping("/{prop_id}")
    public ResponseEntity<PropuestaDto> obtenerPropuesta(
            @CookieValue String token,
            @PathVariable String prop_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.ok(this.propuestaService.obtenerPorId(prop_id, perfilId));
    }

    /**
     * Acepta una propuesta de intercambio. Ejecuta el intercambio de figuritas
     * entre el autor y el destinatario.
     *
     * @param token   token JWT del que se extrae el identificador del perfil que acepta
     * @param prop_id identificador de la propuesta a aceptar
     * @return 204 No Content si la operación se realizó correctamente
     */
    @PatchMapping("/{prop_id}/aceptar")
    public ResponseEntity<?> aceptar(
        @CookieValue String token,
        @PathVariable String prop_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        propuestaService.aceptar(prop_id, perfilId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rechaza una propuesta de intercambio. Libera las figuritas ofrecidas
     * que estaban reservadas.
     *
     * @param token   token JWT del que se extrae el identificador del perfil que rechaza
     * @param prop_id identificador de la propuesta a rechazar
     * @return 204 No Content si la operación se realizó correctamente
     */
    @PatchMapping("/{prop_id}/rechazar")
    public ResponseEntity<?> rechazar(
        @CookieValue String token,
        @PathVariable String prop_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        propuestaService.rechazar(prop_id, perfilId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancela una propuesta de intercambio. Solo el autor puede cancelarla.
     * Libera las figuritas ofrecidas que estaban reservadas.
     *
     * @param token   token JWT del que se extrae el identificador del perfil autor
     * @param prop_id identificador de la propuesta a cancelar
     * @return 204 No Content si la operación se realizó correctamente
     */
    @PatchMapping("/{prop_id}/cancelar")
    public ResponseEntity<?> cancelar(
        @CookieValue String token,
        @PathVariable String prop_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        propuestaService.cancelar(prop_id, perfilId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene las propuestas de intercambio del perfil autenticado, de forma paginada
     * y filtradas por tipo (ENVIADAS o RECIBIDAS).
     *
     * @param token   token JWT del que se extrae el identificador del perfil
     * @param filtros criterios de filtrado (tipo, paginación)
     * @return 200 OK con la página de intercambios encontrados
     */
    @GetMapping()
    public ResponseEntity<PaginaResultado<IntercambioDto>> obtenerPropuestas(
        @CookieValue String token,
        @ModelAttribute PropuestasFiltro filtros
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.ok(this.propuestaService.buscarPropuestas(perfilId, filtros));
    }

}
