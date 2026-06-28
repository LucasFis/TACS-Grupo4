package app.controllers;

import app.dto.FiguritaDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.filtros.FiguritasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.MetodoIntercambio;
import app.servicios.ServicioFigurita;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ControladorFigurita {
    private final ServicioFigurita figuritaService;

    /**
     * Obtiene figuritas intercambiables de forma paginada. Si se proporciona
     * un término de búsqueda {@code q}, realiza una búsqueda por texto libre;
     * en caso contrario, aplica los filtros individuales (número, selección,
     * jugador, tipo de intercambio).
     *
     * @param q             término de búsqueda por texto libre (opcional)
     * @param numero        número de la figurita (opcional)
     * @param seleccion     selección o equipo de la figurita (opcional)
     * @param jugador       nombre del jugador (opcional)
     * @param tipo          lista de métodos de intercambio (opcional)
     * @param pagina        número de página (default: 0)
     * @param tamanioPagina tamaño de página, acotado a 40 como máximo (default: 12)
     * @return 200 OK con la página de figuritas intercambiables encontradas
     */
    @GetMapping("/figuritas/intercambiables")
    public ResponseEntity<PaginaResultado<FiguritaIntercambiableDto>> obtenerFiguritas(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer numero,
        @RequestParam(required = false) String seleccion,
        @RequestParam(required = false) String jugador,
        @RequestParam(required = false) List<MetodoIntercambio> tipo,
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "12") int tamanioPagina
    ) {
        int tamanioDePaginaAcotado = Math.min(tamanioPagina, 40);
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(
                figuritaService.buscarPorQuery(q, tipo, pagina, tamanioDePaginaAcotado)
            );
        }
        return ResponseEntity.ok(
            figuritaService.buscarFiguritas(numero, seleccion, jugador, tipo, pagina, tamanioDePaginaAcotado)
        );
    }

    /**
     * Obtiene la lista de figuritas base (no intercambiables) aplicando los filtros
     * proporcionados.
     *
     * @param filtros criterios de filtrado (número, selección, jugador, etc.)
     * @return 200 OK con la lista de figuritas base encontradas
     */
    @GetMapping("/figuritas")
    public ResponseEntity<List<FiguritaDto>> obtenerFiguritasBase(
        @ModelAttribute FiguritasFiltro filtros
    ) {
        return ResponseEntity.ok(this.figuritaService.buscarFiguritasBase(filtros));
    }
}
