package app.dto;

import app.model.entities.EstadoProceso;
import app.model.entities.Propuesta;
import app.model.entities.Figurita;
import lombok.Getter;
import java.util.List;

@Getter
public class PropuestaDto {
  private String id;
  private String autorId;
  private String destinatarioId;
  private String figuritaBuscadaId;
  private List<String> figuritasOfrecidasIds;
  private EstadoProceso estado;

  public PropuestaDto(Propuesta propuesta) {
    this.id = propuesta.getId();
    this.autorId = propuesta.getAutor().getId();
    this.destinatarioId = propuesta.getDestinatario().getId();
    this.figuritaBuscadaId = propuesta.getFiguritaBuscada().getId();
    this.figuritasOfrecidasIds = propuesta.getFiguritasOfrecidas().stream().map(Figurita::getId)
        .toList();
    this.estado = propuesta.obtenerEstadoActual().getValor();
  }
}