package app.dto;

import app.model.entities.Calificacion;
import lombok.Getter;

@Getter
public class CalificacionDto {
  private String id;
  private String autorId;
  private Integer valor;
  private String descripcion;
  private Number calificacionFinal;

  public CalificacionDto(Calificacion c, Number calificacionFinal) {
    this.id = c.getId();
    this.autorId = c.getAutor().getId();
    this.valor = c.getValor();
    this.descripcion = c.getDescripcion();
    this.calificacionFinal = calificacionFinal;
  }
}
