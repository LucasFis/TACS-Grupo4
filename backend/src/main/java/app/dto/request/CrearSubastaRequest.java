package app.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CrearSubastaRequest {
  @JsonProperty("figurita_id")
  private String figuritaId;

  @JsonProperty("duracion")
  private Number duracion;
}
