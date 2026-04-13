package app.dto;

import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OperacionesDto {

    @JsonProperty("figuritas_publicadas")
    List<FiguritaIntercambiable> figuritasPublicadas;

    @JsonProperty("propuestas_enviadas")
    List<Propuesta> propuestasEnviadas;

    @JsonProperty("propuestas_recibidas")
    List<Propuesta> propuestasRecibidas;

    @JsonProperty("subastas_activas")
    List<Subasta> subastasActivas;
}
