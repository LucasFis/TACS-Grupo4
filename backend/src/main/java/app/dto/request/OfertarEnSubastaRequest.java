package app.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class OfertarEnSubastaRequest {
  @JsonProperty("usuario_id")
  private String usuarioId;
  @JsonProperty("figuritas_ofrecidas_id")
  private List<String> figuritasOfrecidasId;
}
