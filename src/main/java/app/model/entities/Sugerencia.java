package app.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Sugerencia {
  Usuario usuarioSugerido;
  List<Figurita> figuritasSugeridas;
}
