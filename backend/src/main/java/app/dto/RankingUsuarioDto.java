package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RankingUsuarioDto {
  private String perfilId;
  private String nombre;
  private double valor;
  private String detalle;
}
