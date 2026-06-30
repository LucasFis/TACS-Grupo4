package app.controllers;

import app.dto.CalificacionDto;
import app.dto.ContadorDto;
import app.dto.NotificacionDto;
import app.dto.PerfilDto;
import app.dto.SugerenciaDto;
import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CalificacionRequest;
import app.dto.request.PerfilRequest;
import app.servicios.ServicioJwt;
import app.servicios.ServicioPerfil;
import java.util.List;

import app.servicios.ServicioSugerencia;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/sugerencias")
@RequiredArgsConstructor
public class ControladorSugerencia {

  private final ServicioSugerencia sugerenciaService;
  private final ServicioJwt servicioJwt;

  /**
   * Obtiene sugerencias de intercambio para el perfil autenticado,
   * basándose en su colección y los filtros proporcionados.
   *
   * @param token  token JWT del que se extrae el identificador del perfil
   * @param filtro criterios de filtrado y paginación de las sugerencias
   * @return 200 OK con la página de sugerencias encontradas
   */
  @GetMapping
  public ResponseEntity<PaginaResultado<SugerenciaDto>> obtenerSugerencias(
      @CookieValue String token,
      @ModelAttribute SugerenciasFiltro filtro
  ) {
    String perfilId = this.servicioJwt.getPerfilId(token);
    PaginaResultado<SugerenciaDto> sugerenciasDto = this.sugerenciaService.obtenerSugerencias(perfilId, filtro);

    return ResponseEntity.ok().body(sugerenciasDto);
  }

  @PatchMapping("/{sugerenciaId}/favorito")
  public ResponseEntity<Void> alternarFavorito(@CookieValue String token, @PathVariable String sugerenciaId) {
    String perfilId = this.servicioJwt.getPerfilId(token);
    this.sugerenciaService.alternarFavorito(sugerenciaId, perfilId);
    return ResponseEntity.noContent().build();
  }
}