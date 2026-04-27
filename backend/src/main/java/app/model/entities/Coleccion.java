package app.model.entities;

import app.exceptions.FiguritaDuplicadaException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Coleccion {

  private String id;
  private List<Figurita> faltantes = new ArrayList<Figurita>();
  private List<FiguritaIntercambiable> repetidas = new ArrayList<FiguritaIntercambiable>();

  public Coleccion(String id) {
    this.id = id;
  }

  public void agregarFaltante(Figurita faltante) {
    if(tieneFaltante(faltante)) {
      throw new FiguritaDuplicadaException("Figurita ya listada como faltante");
    }

    this.faltantes.add(faltante);
  }

  public void agregarRepetida(FiguritaIntercambiable repetida) {

    for (FiguritaIntercambiable f : repetidas) {
      if (f.getFigurita().getId()
          .equals(repetida.getFigurita().getId())) {

        f.setCantidadExistente(f.getCantidadExistente() + repetida.getCantidadExistente());
        return;
      }
    }

    repetidas.add(repetida);
  }

  public boolean tieneFaltante(Figurita figurita) {
    return this.faltantes.contains(figurita);
  }


}
