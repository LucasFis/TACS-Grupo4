package app.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OfertarEnSubastaRequest {
  @JsonProperty("figuritas_ofrecidas_id")
  @NotEmpty
  private List<String> figuritasOfrecidasId;
}
