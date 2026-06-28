package app.model.entities.filtros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.dto.filtros.FiguritasFiltro;
import org.junit.jupiter.api.Test;

class FiguritasFiltroTest {

  @Test
  void deberiaCrearRecordCorrectamente() {
    FiguritasFiltro filtro = new FiguritasFiltro(
        "abc123",
        10,
        "ARGENTINA",
        "Messi",
        null,
        null,
        null
    );

    assertEquals("abc123", filtro.id());
    assertEquals(10, filtro.numero());
    assertEquals("ARGENTINA", filtro.seleccion());
    assertEquals("Messi", filtro.jugador());
  }

  @Test
  void deberiaAceptarValoresNull() {
    FiguritasFiltro filtro = new FiguritasFiltro(null, null, null, null, null, null, null);

    assertNull(filtro.id());
    assertNull(filtro.numero());
    assertNull(filtro.seleccion());
    assertNull(filtro.jugador());
  }

  @Test
  void deberiaSerIgualSiLosValoresSonIguales() {
    FiguritasFiltro a = new FiguritasFiltro("1", 10, "ARGENTINA", "Messi", null, null, null);
    FiguritasFiltro b = new FiguritasFiltro("1", 10, "ARGENTINA", "Messi", null, null, null);

    assertEquals(a, b);
  }
}