package app.repositories.impl;

import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.Seleccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RepositorioFiguritasEnMemoriaTest {

  private RepositorioFiguritasEnMemoria repositorio;

  @BeforeEach
  void setUp() {
    repositorio = new RepositorioFiguritasEnMemoria();
  }

  @Test
  void findByIdNoEncuentraYtiraExcepcion() {
    Figurita messi = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);

    repositorio.guardar(messi);

    assertThrows(RuntimeException.class, () -> {repositorio.buscarPorId("11");});
  }

  @Test
  void findByIdValido() {
    Figurita messi = new Figurita("ARG-10", 10, "Messi", Seleccion.ARGENTINA);

    repositorio.guardar(messi);

    assertEquals(messi.getId(), repositorio.buscarPorId("ARG-10").getId());
  }
}
