package app.model.entities.filtros;

import app.dto.filtros.FaltantesFiltro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FaltantesFiltrosTest {

  @Test
  void deberiaCrearRecordCorrectamente() {
    FaltantesFiltro filtro = new FaltantesFiltro(10, 2, null);

    assertEquals(10, filtro.limite());
    assertEquals(2, filtro.pagina());
  }

  @Test
  void deberiaSerIgualSiLosValoresSonIguales() {
    FaltantesFiltro a = new FaltantesFiltro(10, 2, null);
    FaltantesFiltro b = new FaltantesFiltro(10, 2, null);

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  void deberiaSerDistintoSiCambianValores() {
    FaltantesFiltro a = new FaltantesFiltro(10, 2, null);
    FaltantesFiltro b = new FaltantesFiltro(20, 1, null);

    assertNotEquals(a, b);
  }
}
