package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EstadisticasDto {
    private long totalUsuarios;
    private long totalFiguritasPublicadas;
    private int totalPropuestas;
    private int totalSubastasActivas;
    private PropuestasPorEstadoDto propuestasPorEstado;
    private FiguritasPorModalidadDto figuritasPorModalidad;
    private RankingsDto rankings;
}
