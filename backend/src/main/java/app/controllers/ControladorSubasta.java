package app.controllers;

import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearSubastaRequest;
import app.dto.request.EditarOfertaRequest;
import app.dto.request.OfertarEnSubastaRequest;
import app.dto.subasta.SubastaDto;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSubasta;
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

    @PostMapping
    public ResponseEntity<Void> crearSubasta(
        @CookieValue String token,
        @RequestBody CrearSubastaRequest body
    ) {
        String perfilId = this.obtenerPerfilIdDeCookie(token);
        this.subastaService.crearSubasta(
            perfilId,
            body.getFiguritaId(),
            body.getDuracionEnHoras(),
            body.getFiguritasDeseadasIds(),
            body.getCalificacionMinima()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sub_id}/ofertas")
    public ResponseEntity<Void> ofertarEnSubasta(
        @CookieValue String token,
        @PathVariable String sub_id,
        @RequestBody OfertarEnSubastaRequest body
    ) {
        String perfilId = this.obtenerPerfilIdDeCookie(token);
        this.subastaService.ofertarEnSubasta(perfilId, sub_id, body.getFiguritasOfrecidasId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{sub_id}/ofertas/{oferta_id}")
    public ResponseEntity<Void> editarOfertaEnSubasta(
        @CookieValue String token,
        @PathVariable String sub_id,
        @PathVariable String oferta_id,
        @RequestBody EditarOfertaRequest body
    ) {
        String perfilId = this.obtenerPerfilIdDeCookie(token);
        this.subastaService.editarOfertaEnSubasta(perfilId, sub_id, oferta_id, body);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{sub_id}/ofertas/{oferta_id}/cancelar")
    public ResponseEntity<Void> cancelarOferta(
        @CookieValue String token,
        @PathVariable String sub_id,
        @PathVariable String oferta_id
    ) {
        String perfilId = this.obtenerPerfilIdDeCookie(token);
        this.subastaService.cancelarOferta(perfilId, sub_id, oferta_id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sub_id}/ofertas/{oferta_id}/seleccionar")
    public ResponseEntity<Void> seleccionarOferta(
        @PathVariable String sub_id,
        @PathVariable String oferta_id
    ) {
        this.subastaService.seleccionarOferta(sub_id, oferta_id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sub_id}/ofertas/{oferta_id}/rechazar")
    public ResponseEntity<Void> rechazarOferta(
        @PathVariable String sub_id,
        @PathVariable String oferta_id
    ) {
        this.subastaService.rechazarOferta(sub_id, oferta_id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sub_id}/cancelar")
    public ResponseEntity<Void> cancelarSubasta(
        @PathVariable String sub_id
    ) {
        this.subastaService.cancelarSubasta(sub_id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sub_id}/cerrar")
    public ResponseEntity<Void> cerrarSubasta(
        @PathVariable String sub_id
    ) {
        this.subastaService.cerrarSubasta(sub_id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PaginaResultado<?>> obtenerSubastas(
        @CookieValue("token") String token,
        @ModelAttribute SubastasFiltro filtros
        ) {
        return ResponseEntity.ok(this.subastaService.obtenerSubastas(filtros));
    }

  @GetMapping("/{sub_id}")
  public ResponseEntity<SubastaDto> obtenerSubasta(
      @PathVariable String sub_id
  ) {
    SubastaDto subasta = this.subastaService.obtenerSubasta(sub_id);

    return ResponseEntity.ok().body(subasta);
  }

  private String obtenerPerfilIdDeCookie(String token) {
    return this.servicioJwt.getPerfilId(token);
  }
}