package app.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
@Getter
public class CrearPropuestaRequest {
  @JsonProperty("autor_id")
  private String autorId;

  @JsonProperty("destinatario_id")
  private String destinatarioId;

  @JsonProperty("figurita_buscada_id")
  private String figuritaBuscadaId;

  @JsonProperty("figuritas_ofrecidas_ids")
  private List<String> figuritasOfrecidasIds;
}