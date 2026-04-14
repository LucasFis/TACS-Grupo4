package app.dto;

import app.model.entities.Figurita;
import app.model.entities.Sugerencia;

import java.util.List;

public class SugerenciaDto {
  UsuarioDto usuario;
  List<Figurita> figuritas;

  public SugerenciaDto(Sugerencia sugerencia) {
    this.usuario = new UsuarioDto(sugerencia.getUsuarioSugerido());
    this.figuritas = sugerencia.getFiguritasSugeridas();
  }
}
