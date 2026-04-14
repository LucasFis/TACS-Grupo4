package app.dto;

import app.model.entities.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioDto {
  private String id;
  private String nombre;

  public UsuarioDto(Usuario usuario) {
    this.id = usuario.getId();
    this.nombre = usuario.getNombre();
  }
}
