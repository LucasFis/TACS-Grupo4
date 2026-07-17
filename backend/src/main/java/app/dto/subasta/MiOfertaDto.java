package app.dto.subasta;

import app.dto.FiguritaDto;
import app.model.entities.EstadoProceso;
import app.model.entities.Propuesta;
import java.util.List;
import lombok.Getter;

@Getter
public class MiOfertaDto {
  private String id;
  private List<FiguritaDto> figuritasOfrecidas;
  private boolean seleccionada;
  public MiOfertaDto(Propuesta propuesta) {
    this.id = propuesta.getId();
    this.figuritasOfrecidas = propuesta.getFiguritasOfrecidas().stream()
        .map(FiguritaDto::new)
        .toList();
    this.seleccionada = propuesta.getEstadoActual().getValor() == EstadoProceso.SELECCIONADO
        || propuesta.getEstadoActual().getValor() == EstadoProceso.ACEPTADO;
  }

}
