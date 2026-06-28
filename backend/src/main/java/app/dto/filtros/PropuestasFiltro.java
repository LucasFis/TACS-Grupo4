package app.dto.filtros;

import app.model.entities.EstadoProceso;

public record PropuestasFiltro(
    String tipo,
    Integer pagina,
    Integer limite,
    EstadoProceso estado
) {
  public PropuestasFiltro {
    if(limite == null){
      limite = 10;
    }
    if(pagina == null){
      pagina = 1;
    }
  }
}
