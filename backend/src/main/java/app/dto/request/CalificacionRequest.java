package app.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CalificacionRequest {

  @JsonProperty("valor")
  private Integer valor;

  @JsonProperty("descripcion")
  private String descripcion;
}
