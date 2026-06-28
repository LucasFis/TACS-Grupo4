package app.repositories.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.MongoTestBase;
import app.model.entities.Figurita;
import app.model.entities.Seleccion;
import app.dto.filtros.FiguritasFiltro;
import org.junit.jupiter.api.Test;

public class RepositorioFiguritasTest extends MongoTestBase {

  @Test
  void buscarPorId_idValido_retornaFigurita() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());

    assertEquals("ARG-10", repositorioFiguritas.buscarPorId("ARG-10").getId());
  }

  @Test
  void buscarPorId_idInexistente_lanzaExcepcion() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());

    assertThrows(RuntimeException.class, () -> repositorioFiguritas.buscarPorId("INEXISTENTE"));
  }

  @Test
  void buscarConFiltros_sinFiltros_retornaTodo() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());
    repositorioFiguritas.guardar(Figurita.builder().id("FRA-7").numero(7).jugador("Mbappé").seleccion(Seleccion.FRANCIA).build());

    var resultado = repositorioFiguritas.buscarConFiltros(new FiguritasFiltro(null, null, null, null, null, null, null));

    assertEquals(2, resultado.size());
  }

  @Test
  void buscarConFiltros_porNumero_retornaCoincidencia() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());
    repositorioFiguritas.guardar(Figurita.builder().id("FRA-7").numero(7).jugador("Mbappé").seleccion(Seleccion.FRANCIA).build());

    var resultado = repositorioFiguritas.buscarConFiltros(new FiguritasFiltro(null, 10, null, null, null, null, null));

    assertEquals(1, resultado.size());
    assertEquals("ARG-10", resultado.get(0).getId());
  }

  @Test
  void buscarConFiltros_porSeleccion_retornaCoincidencia() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());
    repositorioFiguritas.guardar(Figurita.builder().id("FRA-7").numero(7).jugador("Mbappé").seleccion(Seleccion.FRANCIA).build());

    var resultado = repositorioFiguritas.buscarConFiltros(new FiguritasFiltro(null, null, "ARGENTINA", null, null, null, null));

    assertEquals(1, resultado.size());
    assertEquals("ARG-10", resultado.get(0).getId());
  }

  @Test
  void buscarConFiltros_porJugadorExacto_retornaCoincidencia() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());

    var resultado = repositorioFiguritas.buscarConFiltros(new FiguritasFiltro(null, null, null, "Messi", null, null, null));

    assertEquals(1, resultado.size());
  }

  @Test
  void buscarConFiltros_porJugadorParcial_retornaCoincidencia() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Lionel Messi").seleccion(Seleccion.ARGENTINA).build());
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-11").numero(11).jugador("Di María").seleccion(Seleccion.ARGENTINA).build());

    var resultado = repositorioFiguritas.buscarConFiltros(new FiguritasFiltro(null, null, null, "messi", null, null, null));

    assertEquals(1, resultado.size());
    assertEquals("ARG-10", resultado.get(0).getId());
  }

  @Test
  void buscarConFiltros_porJugadorCaseInsensitive_retornaCoincidencia() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());

    var resultado = repositorioFiguritas.buscarConFiltros(new FiguritasFiltro(null, null, null, "MESSI", null, null, null));

    assertEquals(1, resultado.size());
  }

  @Test
  void buscarConFiltros_sinResultados_daListaVacia() {
    repositorioFiguritas.guardar(Figurita.builder().id("ARG-10").numero(10).jugador("Messi").seleccion(Seleccion.ARGENTINA).build());

    assertEquals(0, repositorioFiguritas.buscarConFiltros(new FiguritasFiltro(null, 99, null, null, null, null, null)).size());
  }
}
