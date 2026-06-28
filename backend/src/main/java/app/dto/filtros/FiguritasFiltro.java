package app.dto.filtros;

import app.model.entities.MetodoIntercambio;
import java.util.List;

public record FiguritasFiltro(
    String id,
    Integer numero,
    String seleccion,
    String jugador,
    List<MetodoIntercambio> tipos,
    Integer pagina,
    Integer tamanioPagina
) {

  private static final int TAMANIO_PAGINA_POR_DEFECTO = 40;
  private static final int TAMANIO_PAGINA_MAXIMO = 40;

  public int paginaEfectiva() {
    return pagina != null ? pagina : 0;
  }

  public int tamanioPaginaEfectivo() {
    return tamanioPagina != null ? Math.min(tamanioPagina, TAMANIO_PAGINA_MAXIMO) : TAMANIO_PAGINA_POR_DEFECTO;
  }
}
