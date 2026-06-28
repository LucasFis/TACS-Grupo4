package app.model.entities;

import app.exceptions.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "colecciones")
public class Coleccion {
  @Id
  private String id;

  @DBRef
  private List<Figurita> faltantes = new ArrayList<Figurita>();

  private List<FiguritaIntercambiable> repetidas = new ArrayList<FiguritaIntercambiable>();

  public Coleccion(String id) {
    this.id = id;
  }

  /**
   * Agrega una figurita a la lista de faltantes.
   * Lanza {@link BadRequestException} si ya está registrada como faltante.
   */
  public void agregarFaltante(Figurita faltante) {
    if (tieneFaltante(faltante)) {
      throw new BadRequestException("Figurita ya listada como faltante");
    }

    this.faltantes.add(faltante);
  }

  /**
   * Agrega una figurita repetida a la colección. Si ya existe una entrada para
   * esa figurita, acumula la cantidad en lugar de crear una entrada duplicada.
   */
  public void agregarRepetida(FiguritaIntercambiable repetida) {
    FiguritaIntercambiable existente =
        obtenerRepetida(repetida.getFigurita());

    if (existente != null) {
      existente.setCantidadExistente(
          existente.getCantidadExistente()
              + repetida.getCantidadExistente()
      );
      return;
    }

    this.repetidas.add(repetida);
  }

  /**
   * Remueve una faltante de la lista de figuritas faltantes.
   */
  public void eliminarFaltante(Figurita faltante) {
    if (!tieneFaltante(faltante)) throw new BadRequestException("Faltante no existe: " + faltante.getJugador());
    this.faltantes.removeIf(
        f -> f.getId().equals(faltante.getId())
    );
  }

  /**
   * Remueve una figurita intercambiable de la lista de figuritas intercambiables.
   */
  public void eliminarRepetida(Figurita repetida) {
    this.repetidas.removeIf(r -> r.getFigurita().getId().equals(repetida.getId()));
  }

  /**
   * Descuenta una repetida de la lista de figuritas repetidas. Si queda con cantidad existente
   * igual a cero la remueve de la lista.
   */
  public void descontarRepetida(Figurita repetida) {
    if(!this.tieneRepetida(repetida)) throw new BadRequestException("Repetida no existe: " + repetida.getJugador());

    FiguritaIntercambiable repetidaAmodificar = this.obtenerRepetida(repetida);

    repetidaAmodificar.cambioConcretado();

    if(repetidaAmodificar.getCantidadExistente() == 0) {
      this.eliminarRepetida(repetida);
    }
  }

  /**
   * Valida que una figurita este en la lista de figuritas faltantes.
   */
  public boolean tieneFaltante(Figurita figurita) {
    return !this.faltantes.stream()
        .filter(p -> p.getId().equals(figurita.getId()))
        .toList()
        .isEmpty();
  }

  /**
   * Valida que una figurita este en la lista de figuritas intercambiables.
   */
  public boolean tieneRepetida(Figurita figurita) {
    return !this.repetidas.stream()
        .filter(i -> i.getFigurita().getId().equals(figurita.getId()))
        .toList()
        .isEmpty();
  }

  /**
   * Reserva cada figurita de la lista si existe en la colección,
   * usando el método de intercambio indicado.
   */
  public void reservarRepetidas(List<Figurita> repetidas, MetodoIntercambio metodo) {
    repetidas.forEach(figurita -> {

      if (tieneRepetida(figurita)) {

        FiguritaIntercambiable repetida = obtenerRepetida(figurita);

        repetida.reservar(metodo);
      }
    });
  }

  /**
   * Elimina la reserva de la lista de figuritas dadas.
   */
  public void sacarReservasRepetidas(List<Figurita> repetidas) {
    repetidas.forEach(figurita -> {
      if (tieneRepetida(figurita)) {
        FiguritaIntercambiable repetida = obtenerRepetida(figurita);
        repetida.eliminarReserva();
      }
    });
  }

  private FiguritaIntercambiable obtenerRepetida(Figurita figurita) {
    return this.repetidas.stream()
        .filter(r -> r.getFigurita().getId().equals(figurita.getId()))
        .findFirst()
        .orElse(null);
  }
}
