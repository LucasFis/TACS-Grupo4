package app.model.entities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ColeccionTest {
  Figurita messi = new Figurita("ARG-10", 10, "Messi",     Seleccion.ARGENTINA);
  Figurita diMaria = new Figurita("ARG-11", 11, "Di María",  Seleccion.ARGENTINA);

  @Test
  void agregarNuevaFaltante() {
    Coleccion coleccion = new Coleccion("10");
    coleccion.agregarFaltante(messi);
    coleccion.agregarFaltante(diMaria);

    assertTrue(coleccion.getFaltantes().contains(messi));
    assertEquals(2, coleccion.getFaltantes().size());
  }

  @Test
  void agregarNuevaFaltante_duplicada_tiraError() {
    Coleccion coleccion = new Coleccion("10");
    coleccion.agregarFaltante(messi);

    assertThrows(RuntimeException.class, () -> coleccion.agregarFaltante(messi));
  }

  @Test
  void agregarNuevaRepetida() {
    Coleccion coleccion = new Coleccion("10");
    List<MetodoIntercambio> metodosIntercambio = new ArrayList<>(
        Arrays.asList(MetodoIntercambio.SUBASTA)
    );

    FiguritaIntercambiable repetida = new FiguritaIntercambiable(
        messi, 2, metodosIntercambio);

    FiguritaIntercambiable repetida2 = new FiguritaIntercambiable(
        diMaria, 2, metodosIntercambio);
    coleccion.agregarRepetida(repetida);

    coleccion.agregarRepetida(repetida);
    coleccion.agregarRepetida(repetida2);

    assertTrue(coleccion.getRepetidas().contains(repetida));
    assertTrue(coleccion.getRepetidas().contains(repetida2));
    assertEquals(2, coleccion.getRepetidas().size());
  }

  @Test
  void agregarNuevaRepetidaDuplicada() {
    Coleccion coleccion = new Coleccion("10");
    List<MetodoIntercambio> metodosIntercambio = new ArrayList<>(
        Arrays.asList(MetodoIntercambio.SUBASTA)
    );

    FiguritaIntercambiable repetida = new FiguritaIntercambiable(
        messi, 2, metodosIntercambio);

    FiguritaIntercambiable repetida2 = new FiguritaIntercambiable(
        messi, 2, metodosIntercambio);
    coleccion.agregarRepetida(repetida);

    coleccion.agregarRepetida(repetida);

    assertTrue(coleccion.getRepetidas().contains(repetida));
    assertEquals(1, coleccion.getRepetidas().size());
    assertEquals(4, repetida.getCantidadExistente());
  }
}
