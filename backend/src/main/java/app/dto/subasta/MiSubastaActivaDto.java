package app.dto.subasta;

import app.dto.FiguritaDto;
import app.model.entities.EstadoProceso;
import app.model.entities.Subasta;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import java.time.Duration;

@Getter
public class MiSubastaActivaDto {
  private String id;
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaCierre;
  private FiguritaDto figuritaSubastada;
  private List<OfertaSubastaDto> ofertas;
  private Long tiempoRestante;

  public MiSubastaActivaDto(Subasta subasta) {
    this.id = subasta.getId();
    this.fechaInicio = subasta.getFechaInicio();
    this.fechaCierre = subasta.getFechaCierre();
    this.figuritaSubastada = new FiguritaDto(subasta.getFiguritaSubastada());
    //para que no me traiga las ofertas rechazadas o canceladas
    this.ofertas = subasta.getOfertas().stream()
        .filter(o -> !Set.of(EstadoProceso.CANCELADO, EstadoProceso.RECHAZADO)
            .contains(o.getEstadoActual().getValor()))
        .map(OfertaSubastaDto::new).toList();
    this.tiempoRestante =
            Duration.between(LocalDateTime.now(), subasta.getFechaCierre()).getSeconds();
  }
}
