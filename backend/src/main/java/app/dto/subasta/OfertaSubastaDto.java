package app.dto.subasta;

import app.dto.FiguritaDto;
import app.dto.PerfilDto;
import app.model.entities.Propuesta;
import app.model.entities.EstadoProceso;
import java.util.List;
import lombok.Getter;

@Getter
public class OfertaSubastaDto {
  private String id;
  private PerfilDto autor;
  private List<FiguritaDto> figuritasOfrecidas;
  private boolean seleccionada;
  private EstadoProceso estado;

  public OfertaSubastaDto(Propuesta propuesta) {
    this.id = propuesta.getId();
    this.autor = new PerfilDto(propuesta.getAutor());
    this.figuritasOfrecidas = propuesta.getFiguritasOfrecidas().stream()
        .map(FiguritaDto::new)
        .toList();
    EstadoProceso estado = propuesta.getEstadoActual().getValor();

    this.seleccionada =
            estado == EstadoProceso.SELECCIONADO
                    || estado == EstadoProceso.ACEPTADO;
    this.estado = estado;
  }
}