package app.dto.subasta;

import app.dto.FiguritaDto;
import app.model.entities.EstadoProceso;
import app.model.entities.Subasta;
import java.time.LocalDateTime;
import lombok.Getter;
import java.time.Duration;

@Getter
public class MiSubastaFinalizadaDto {
  private String id;
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaCierre;
  private FiguritaDto figuritaSubastada;
  private OfertaSubastaDto ofertaGanadora;
  private boolean yaCalificado;
  private String finalizadaHace;

  public MiSubastaFinalizadaDto(Subasta subasta, boolean yaCalificado) {
    this.id = subasta.getId();
    this.fechaInicio = subasta.getFechaInicio();
    this.fechaCierre = subasta.getFechaCierre();
    this.figuritaSubastada = new FiguritaDto(subasta.getFiguritaSubastada());
    this.ofertaGanadora = subasta.getOfertas().stream()
        .filter(p -> p.getEstadoActual().getValor() == EstadoProceso.ACEPTADO)
        .findFirst()
        .map(OfertaSubastaDto::new)
        .orElse(null);
    this.yaCalificado = yaCalificado;

      Duration d = Duration.between(subasta.getFechaCierre(), LocalDateTime.now());

      if (d.toDays() > 0) {
          this.finalizadaHace = d.toDays() + "d";
      } else if (d.toHours() > 0) {
          this.finalizadaHace = d.toHours() + "h";
      } else if (d.toMinutes() > 0) {
          this.finalizadaHace = d.toMinutes() + "m";
      } else {
          this.finalizadaHace = "Ahora";
      }
  }
}
