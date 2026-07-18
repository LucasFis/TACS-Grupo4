package app.dto.subasta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidarCondicionesDto {
  private boolean puedeOfertar;
  private String motivo;
}
