package app.dto;

import app.model.entities.EstadoProceso;
import app.model.entities.Propuesta;
import app.model.entities.Figurita;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PropuestaDto {
  private String id;
  private PerfilDto autor;
  private PerfilDto destinatario;
  private Figurita figuritaBuscada;
  private List<Figurita> figuritasOfrecidas;
  private EstadoProceso estado;
  private String tipo;

  public PropuestaDto(Propuesta propuesta) {
    this.id = propuesta.getId();
    this.autor = new PerfilDto(propuesta.getAutor());
    this.destinatario = new PerfilDto(propuesta.getDestinatario());
    this.figuritaBuscada = propuesta.getFiguritaBuscada();
    this.figuritasOfrecidas = propuesta.getFiguritasOfrecidas();
    this.estado = propuesta.getEstadoActual().getValor();
  }

  //Hago uno nuevo para que no rompa todo lo que hay con el viejo y el nuevo llama al viejo caundo interesa el tipo.
  public PropuestaDto(Propuesta propuesta, String tipo) {
      this(propuesta);
      this.tipo = tipo;
  }

}