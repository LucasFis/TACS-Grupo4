package app.dto.request;

import app.model.entities.MetodoIntercambio;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record EditarRepetidaRequest(
    @JsonProperty("cantidad_nueva")
    int cantidadNueva,
    @NotEmpty
    List<MetodoIntercambio> metodos
) {
}
