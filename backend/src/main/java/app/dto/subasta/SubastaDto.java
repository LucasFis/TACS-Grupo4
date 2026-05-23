package app.dto.subasta;

import app.model.entities.EstadoProceso;
import app.dto.PerfilDto;
import app.dto.PropuestaDto;
import app.model.entities.Figurita;
import app.model.entities.Subasta;
import lombok.Getter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SubastaDto {
  String id;
  PerfilDto perfil;
  LocalDateTime inicio;
  LocalDateTime cierre;
  Number tiempoRestante;
  Figurita figurita;
  List<Figurita> figuritasSolicitadas;
  Integer calificacionMinimaSolicitada;
  List<PropuestaDto> ofertas;

  public SubastaDto(Subasta subasta) {
    this.id = subasta.getId();
    this.perfil = new PerfilDto(subasta.getAutor());
    this.inicio = subasta.getFechaInicio();
    this.cierre = subasta.getFechaCierre();

    Duration duracion = Duration.between(
        LocalDateTime.now(),
        subasta.getFechaCierre()
    );

    this.tiempoRestante = Math.max(0, duracion.toSeconds());
    this.figurita = subasta.getFiguritaSubastada();

    this.ofertas = subasta.getOfertas().stream()
        .filter(o -> o.obtenerEstadoActual().getValor() != EstadoProceso.CANCELADO)
        .map(PropuestaDto::new)
        .toList();

    this.figuritasSolicitadas = subasta.getFiguritasSolicitadas();
    this.calificacionMinimaSolicitada = subasta.getCalificacionMinimaSolicitada();
  }
}