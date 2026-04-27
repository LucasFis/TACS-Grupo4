package app.dto;

import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
//TODO no se corrige porque este dto dejara de existir, se dividira en mis figuritas, intercambios y subastas
public class OperacionesDto {

    List<FiguritaIntercambiable> figuritasPublicadas;

    List<Propuesta> propuestasEnviadas;

    List<Propuesta> propuestasRecibidas;

    List<Subasta> subastasActivas;
}
