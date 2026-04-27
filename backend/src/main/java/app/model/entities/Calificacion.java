package app.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Calificacion {
  private String id;
  private Perfil autor;
  //valor es un entero de 1 a 5
  private Integer valor;
  private String descripcion;
}
