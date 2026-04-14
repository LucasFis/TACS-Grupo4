package app.dto;

import app.model.entities.Figurita;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import lombok.Getter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SubastaDto {
  UsuarioDto usuario;
  long duracion;
  Figurita figurita;
  PropuestaDto propuestaGanadora;

  public SubastaDto(Subasta subasta) {
    this.usuario = new UsuarioDto(
        subasta.getUsuario().getId(),
        subasta.getUsuario().getNombre()
    );

    Duration duracion = Duration.between(
        subasta.getFechaInicio(),
        subasta.getFechaCierre()
    );

    this.duracion = duracion.toMinutes();
    this.figurita = subasta.getFiguritaSubastada();
    Propuesta propGanadora = subasta.getPropuestaGanadora();

    if(propGanadora == null) {
      this.propuestaGanadora = null;
    }
    else {
      List<Figurita> figuritasOfrecidasDom = new ArrayList<>(propGanadora.getFiguritasOfrecidas());
      this.propuestaGanadora = new PropuestaDto(propGanadora.getId(),
          propGanadora.getUsuarioOrigen().getId(), propGanadora.getUsuarioDestino().getId(),
          propGanadora.getFiguritaBuscada().getId(),
          figuritasOfrecidasDom.stream().map(Figurita::getId).toList(), propGanadora.getEstado());
    }
  }
}
