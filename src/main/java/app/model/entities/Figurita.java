package app.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Figurita {

  private String id;

  private Integer numero;

  private String jugador;

  private Seleccion seleccion;
}
