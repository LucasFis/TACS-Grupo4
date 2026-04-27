package app.model.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetodoIntercambioTest {

  @Test
  void metodoDeIntercambioInvalidoTiraExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> {MetodoIntercambio.fromString("Hola");});
  }

  @Test
  void metodoDeIntercambioValido() {
    assertEquals(MetodoIntercambio.SUBASTA, MetodoIntercambio.fromString("Subasta"));
    assertEquals(MetodoIntercambio.SUBASTA, MetodoIntercambio.fromString("SUBASTA"));
    assertEquals(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.fromString("Intercambio"));
    assertEquals(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.fromString("INTERCAMBIO"));
    assertEquals(MetodoIntercambio.SUBASTA_E_INTERCAMBIO, MetodoIntercambio.fromString("SubasTa_e_intercamBio"));
    assertEquals(MetodoIntercambio.SUBASTA_E_INTERCAMBIO, MetodoIntercambio.fromString("SUBASTA_E_INTERCAMBIO"));
  }
}
