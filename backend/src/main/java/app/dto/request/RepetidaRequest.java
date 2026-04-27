package app.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RepetidaRequest {

  @JsonProperty("cantidad_existente")
  public Integer cantidadExistente;

  @JsonProperty("fig_id")
  public String figId;

  @JsonProperty("modos_intercambio")
  public List<String> modosIntercambio;
}
