package app.dto;

import app.model.entities.Figurita;
import app.model.entities.Sugerencia;
import lombok.Getter;
import java.util.List;

@Getter
public class SugerenciaDto {
  String id;
  PerfilDto autor;
  PerfilDto sugerido;
  List<Figurita> figuritasRecomendadas;
  List<Figurita> figuritasNecesarias;
  Boolean favorito;

  public SugerenciaDto(Sugerencia sugerencia) {
    this.id = sugerencia.getId();
    this.autor = new PerfilDto(sugerencia.getAutor());
    this.sugerido = new PerfilDto(sugerencia.getSugerido());
    this.figuritasRecomendadas = sugerencia.getFiguritasSugeridas();
    this.figuritasNecesarias = sugerencia.getFiguritasNecesarias();
    this.favorito = sugerencia.getFavorito();
  }
}
