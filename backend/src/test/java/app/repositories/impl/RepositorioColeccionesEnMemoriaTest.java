package app.repositories.impl;

import app.model.entities.Coleccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RepositorioColeccionesEnMemoriaTest {

  private RepositorioColeccionesEnMemoria repositorio;

  @BeforeEach
  void setUp() {
    repositorio = new RepositorioColeccionesEnMemoria();
  }

  @Test
  void findByIdNoEncuentraYtiraExcepcion() {
    Coleccion coleccion = new Coleccion("10");

    repositorio.guardar(coleccion);

    assertThrows(RuntimeException.class, () -> {repositorio.buscarPorId("11");});
  }

  @Test
  void findByIdValido() {
    Coleccion coleccion = new Coleccion("10");

    repositorio.guardar(coleccion);

    assertEquals(coleccion.getId(), repositorio.buscarPorId("10").getId());
  }

}
