package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.dto.FiguritaIntercambiableDto;
import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Seleccion;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioFiguritasIntercambiables;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FiguritaServiceTest {
  @Mock
  RepositorioFiguritas repositorioFiguritas;
  @Mock
  RepositorioFiguritasIntercambiables repositorioIntercambiables;
  @InjectMocks
  FiguritaService figuritaService;
  Figurita messi = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);
  FiguritaIntercambiable intercambiable = new FiguritaIntercambiable(
      messi, 2, List.of(MetodoIntercambio.INTERCAMBIO), "1000");
  @Test
  void buscarFiguritasPorSeleccionDevuelveResultados() {
    when(repositorioFiguritas.buscarConFiltros(null, Seleccion.ARGENTINA, null))
        .thenReturn(List.of(messi));
    when(repositorioIntercambiables.buscarPorFiguritaIds(List.of("ARG-10")))
        .thenReturn(List.of(intercambiable));

    List<FiguritaIntercambiableDto> resultado =
        figuritaService.buscarFiguritas(null, Seleccion.ARGENTINA, null);

    assertEquals(1, resultado.size());
    assertEquals("ARG-10", resultado.get(0).getFiguritaId());
    assertEquals("1000", resultado.get(0).getUsuarioId());
  }

  @Test
  void buscarFiguritasSinResultadosLanzaNotFoundException() {
    when(repositorioFiguritas.buscarConFiltros(any(), any(), any()))
        .thenThrow(new NotFoundException("No se encontraron figuritas con esos filtros"));

    assertThrows(NotFoundException.class,
        () -> figuritaService.buscarFiguritas(null, null, null));
  }
}
