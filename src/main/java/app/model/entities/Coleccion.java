package app.model.entities;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Coleccion {

  private List<Figurita> faltantes = new ArrayList<>();
  private List<FiguritaIntercambiable> repetidas = new ArrayList<>();

}
