package app.model.entities.filtros;

import app.dto.filtros.RepetidasFiltro;
import app.model.entities.MetodoIntercambio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepetidasFiltroTest {

  @Test
  void deberiaCrearRecordCorrectamente() {
    RepetidasFiltro filtro = new RepetidasFiltro(
        MetodoIntercambio.SUBASTA,
        "1000",
        5,
        1
    );

    assertEquals(MetodoIntercambio.SUBASTA, filtro.metodoIntercambio());
    assertEquals("1000", filtro.perfilId());
    assertEquals(5, filtro.limite());
    assertEquals(1, filtro.pagina());
  }

  @Test
  void deberiaAceptarValoresNull() {
    RepetidasFiltro filtro = new RepetidasFiltro(null,null, null, null);

    assertNull(filtro.metodoIntercambio());
    assertNull(filtro.perfilId());
    assertNotNull(filtro.limite());
    assertNotNull(filtro.pagina());
  }

  @Test
  void deberiaSerIgualSiLosValoresSonIguales() {
    RepetidasFiltro a = new RepetidasFiltro(MetodoIntercambio.SUBASTA,"1000", 5, 1);
    RepetidasFiltro b = new RepetidasFiltro(MetodoIntercambio.SUBASTA, "1000", 5, 1);

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }
}