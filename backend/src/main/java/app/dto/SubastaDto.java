package app.dto;

import app.model.entities.Figurita;
import app.model.entities.Subasta;
import lombok.Getter;
import java.time.Duration;
import java.util.List;
@Getter
public class SubastaDto {
  PerfilDto perfilDto;
  long duracion;
  Figurita figurita;
  List<PropuestaDto> ofertas;

  public SubastaDto(Subasta subasta) {
    this.perfilDto = new PerfilDto(subasta.getAutor());

    Duration duracion = Duration.between(
        subasta.getFechaInicio(),
        subasta.getFechaCierre()
    );

    this.duracion = duracion.toMinutes();
    this.figurita = subasta.getFiguritaSubastada();

    this.ofertas = subasta.getOfertas().stream()
        .map(PropuestaDto::new)
        .toList();
  }
}