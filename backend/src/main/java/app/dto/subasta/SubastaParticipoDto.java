package app.dto.subasta;

import app.dto.FiguritaDto;
import app.dto.PerfilDto;
import app.dto.PropuestaDto;
import app.model.entities.EstadoProceso;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.Duration;

@Getter
public class SubastaParticipoDto {
  private String id;
  private PerfilDto autor;
  private FiguritaDto figuritaSubastada;
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaCierre;
  MiOfertaDto tuOferta;
  private boolean yaCalificado;
  private String finalizadaHace;
  private long tiempoRestante;

  public SubastaParticipoDto(Subasta subasta, Propuesta tuOferta, boolean yaCalificado) {
    this.id = subasta.getId();
    this.autor = new PerfilDto(subasta.getAutor());
    this.figuritaSubastada = new FiguritaDto(subasta.getFiguritaSubastada());
    this.fechaInicio = subasta.getFechaInicio();
    this.fechaCierre = subasta.getFechaCierre();
    this.tuOferta = new MiOfertaDto(tuOferta);
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

      if (subasta.getFechaCierre().isAfter(LocalDateTime.now())) {
          this.tiempoRestante = Duration.between(
                  LocalDateTime.now(),
                  subasta.getFechaCierre()
          ).getSeconds();
      } else {
          this.tiempoRestante = 0;
      }
  }
}