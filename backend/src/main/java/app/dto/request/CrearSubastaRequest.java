package app.dto.request;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CrearSubastaRequest {

  @NotBlank
  private String figuritaId;

  @Positive
  @NotNull
  private Integer duracionEnHoras;

  private List<String> figuritasDeseadasIds = new ArrayList<>();

  @Min(0)
  @Max(5)
  @NotNull
  private Integer calificacionMinima = 0;

  @AssertTrue(message = "La lista de figuritas deseadas no puede contener nulls")
  private boolean isFiguritasDeseadasIdsValid() {
    return figuritasDeseadasIds == null || figuritasDeseadasIds.stream().noneMatch(Objects::isNull);
  }
}
