package app.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class FaltanteRequest {

  @JsonProperty("fig_id")
  private String figId;

}
