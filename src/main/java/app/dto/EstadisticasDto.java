package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EstadisticasDto {
    private int totalUsuarios;
    private int totalFiguritasPublicadas;
    private int totalPropuestas;
    private int totalSubastasActivas;
}
