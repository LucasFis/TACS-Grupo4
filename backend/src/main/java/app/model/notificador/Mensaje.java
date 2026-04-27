package app.model.notificador;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class Mensaje {
  private String cuerpo;
  private LocalDateTime fecha;
}