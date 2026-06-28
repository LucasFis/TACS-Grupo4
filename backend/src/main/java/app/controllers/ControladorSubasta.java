package app.controllers;

import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearSubastaRequest;
import app.dto.request.EditarOfertaRequest;
import app.dto.request.OfertarEnSubastaRequest;
import app.dto.subasta.SubastaDto;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSubasta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subastas")
@RequiredArgsConstructor
public class ControladorSubasta {
    private final ServicioSubasta subastaService;
    private final ServicioJwt servicioJwt;

    /**
     * Crea una nueva subasta para una figurita repetida del perfil autenticado.
     *
     * @param token token JWT del que se extrae el identificador del perfil
     * @param body  datos de la subasta (figurita, duración, figuritas deseadas, calificación mínima)
     * @return 200 OK si la subasta se creó correctamente
     */
    @PostMapping
    public ResponseEntity<Void> crearSubasta(
        @CookieValue String token,
        @Valid @RequestBody CrearSubastaRequest body
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.crearSubasta(
            perfilId,
            body.getFiguritaId(),
            body.getDuracionEnHoras(),
            body.getFiguritasDeseadasIds(),
            body.getCalificacionMinima()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * Realiza una oferta en una subasta activa ofreciendo figuritas repetidas.
     *
     * @param token  token JWT del que se extrae el identificador del perfil ofertante
     * @param sub_id identificador de la subasta
     * @param body   lista de figuritas ofrecidas
     * @return 200 OK si la oferta se registró correctamente
     */
    @PostMapping("/{sub_id}/ofertas")
    public ResponseEntity<Void> ofertarEnSubasta(
        @CookieValue String token,
        @PathVariable String sub_id,
        @Valid @RequestBody OfertarEnSubastaRequest body
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.ofertarEnSubasta(perfilId, sub_id, body.getFiguritasOfrecidasId());
        return ResponseEntity.ok().build();
    }

    /**
     * Modifica las figuritas ofrecidas en una oferta existente dentro de una subasta activa.
     *
     * @param token     token JWT del que se extrae el identificador del perfil
     * @param sub_id    identificador de la subasta
     * @param oferta_id identificador de la oferta a modificar
     * @param body      nuevas figuritas ofrecidas
     * @return 200 OK si la edición se realizó correctamente
     */
    @PatchMapping("/{sub_id}/ofertas/{oferta_id}")
    public ResponseEntity<Void> editarOfertaEnSubasta(
        @CookieValue String token,
        @PathVariable String sub_id,
        @PathVariable String oferta_id,
        @Valid @RequestBody EditarOfertaRequest body
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.editarOfertaEnSubasta(perfilId, sub_id, oferta_id, body);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancela una oferta existente en una subasta activa, liberando las figuritas reservadas.
     *
     * @param token     token JWT del que se extrae el identificador del perfil
     * @param sub_id    identificador de la subasta
     * @param oferta_id identificador de la oferta a cancelar
     * @return 200 OK si la oferta se canceló correctamente
     */
    @PatchMapping("/{sub_id}/ofertas/{oferta_id}/cancelar")
    public ResponseEntity<Void> cancelarOferta(
        @CookieValue String token,
        @PathVariable String sub_id,
        @PathVariable String oferta_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.cancelarOferta(perfilId, sub_id, oferta_id);
        return ResponseEntity.ok().build();
    }

    /**
     * Selecciona una oferta como ganadora de la subasta.
     *
     * @param token     token JWT del que se extrae el identificador del perfil (autor de la subasta)
     * @param sub_id    identificador de la subasta
     * @param oferta_id identificador de la oferta seleccionada
     * @return 200 OK si la selección se realizó correctamente
     */
    @PatchMapping("/{sub_id}/ofertas/{oferta_id}/seleccionar")
    public ResponseEntity<Void> seleccionarOferta(
        @CookieValue("token") String token,
        @PathVariable String sub_id,
        @PathVariable String oferta_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.seleccionarOferta(perfilId, sub_id, oferta_id);
        return ResponseEntity.ok().build();
    }

    /**
     * Rechaza una oferta en una subasta activa, liberando las figuritas reservadas.
     *
     * @param token     token JWT del que se extrae el identificador del perfil (autor de la subasta)
     * @param sub_id    identificador de la subasta
     * @param oferta_id identificador de la oferta a rechazar
     * @return 200 OK si la oferta se rechazó correctamente
     */
    @PatchMapping("/{sub_id}/ofertas/{oferta_id}/rechazar")
    public ResponseEntity<Void> rechazarOferta(
        @CookieValue("token") String token,
        @PathVariable String sub_id,
        @PathVariable String oferta_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.rechazarOferta(perfilId, sub_id, oferta_id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancela una subasta activa por completo, liberando todas las figuritas reservadas.
     *
     * @param token  token JWT del que se extrae el identificador del perfil (autor de la subasta)
     * @param sub_id identificador de la subasta a cancelar
     * @return 200 OK si la subasta se canceló correctamente
     */
    @PatchMapping("/{sub_id}/cancelar")
    public ResponseEntity<Void> cancelarSubasta(
        @CookieValue("token") String token,
        @PathVariable String sub_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.cancelarSubasta(perfilId, sub_id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cierra una subasta activa. Si hay una oferta seleccionada, ejecuta el intercambio
     * entre el ganador y el autor de la subasta.
     *
     * @param token  token JWT del que se extrae el identificador del perfil (autor de la subasta)
     * @param sub_id identificador de la subasta a cerrar
     * @return 200 OK si la subasta se cerró correctamente
     */
    @PatchMapping("/{sub_id}/cerrar")
    public ResponseEntity<Void> cerrarSubasta(
        @CookieValue("token") String token,
        @PathVariable String sub_id
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        this.subastaService.cerrarSubasta(perfilId, sub_id);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene las subastas del perfil autenticado según los filtros aplicados.
     * El tipo de respuesta varía según el contexto:
     * <ul>
     *   <li>Si {@code filtros.participanteId()} está presente: subastas en las que participó.</li>
     *   <li>Si el estado es {@code ACTIVA}: subastas activas del perfil.</li>
     *   <li>En otro caso: subastas finalizadas del perfil.</li>
     * </ul>
     *
     * @param token   token JWT del que se extrae el identificador del perfil
     * @param filtros criterios de filtrado (estado, participante, paginación)
     * @return 200 OK con la página de subastas según el tipo de vista
     */
    @GetMapping
    public ResponseEntity<PaginaResultado<?>> obtenerSubastas(
        @CookieValue("token") String token,
        @ModelAttribute SubastasFiltro filtros
    ) {
        String perfilId = this.servicioJwt.getPerfilId(token);
        return ResponseEntity.ok(this.subastaService.obtenerSubastas(perfilId, filtros));
    }

    @GetMapping("/{sub_id}")
    public ResponseEntity<SubastaDto> obtenerSubasta(
      @PathVariable String sub_id
    ) {
    SubastaDto subasta = this.subastaService.obtenerSubasta(sub_id);

    return ResponseEntity.ok().body(subasta);
    }
}