package app.model.entities;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EstadoPropuesta {
  private LocalDateTime fecha;
  private EstadoProceso valor;
}
