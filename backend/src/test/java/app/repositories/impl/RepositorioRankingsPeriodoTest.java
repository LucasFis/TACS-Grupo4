package app.repositories.impl;

import app.MongoTestBase;
import app.dto.RankingUsuarioDto;
import app.model.entities.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test reproductor del reporte del evaluador: "Top subastadores ignora el período".
 */
class RepositorioRankingsPeriodoTest extends MongoTestBase {

  @Autowired
  private RepositorioRankingsMongo repositorioRankings;

  private Perfil perfilConObjectId;
  private Perfil perfilConStringId;
  private Figurita figurita;

  @BeforeEach
  void setUp() {
    Coleccion colA = new Coleccion();
    repositorioColecciones.guardar(colA);

    Coleccion colB = new Coleccion();
    repositorioColecciones.guardar(colB);

    Usuario u1 = new Usuario("u1", Rol.USUARIO, "runtime_user", "pass");
    repositorioUsuarios.guardar(u1);

    Usuario u2 = new Usuario("u2", Rol.USUARIO, "lucas_seed", "pass");
    repositorioUsuarios.guardar(u2);

    perfilConObjectId = Perfil.builder()
        .id(new ObjectId().toString())
        .nombre("Usuario Runtime")
        .usuario(u1)
        .coleccion(colA)
        .mediosDeContacto(List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@runtime")))
        .build();
    repositorioPerfiles.guardar(perfilConObjectId);

    perfilConStringId = Perfil.builder()
        .id("lucas-id-001")
        .nombre("Lucas Seed")
        .usuario(u2)
        .coleccion(colB)
        .mediosDeContacto(List.of(new MedioDeContacto(MedioComunicacion.TELEGRAM, "@lucas")))
        .build();
    repositorioPerfiles.guardar(perfilConStringId);

    figurita = Figurita.builder()
        .id("ARG-10")
        .numero(10)
        .jugador("Messi")
        .seleccion(Seleccion.ARGENTINA)
        .build();
    repositorioFiguritas.guardar(figurita);
  }

  @Test
  void topSubastadores_conSubastaFueraDelPeriodo_retornaVacio() {
    LocalDateTime desde = LocalDate.of(2026, 7, 1).atStartOfDay();
    LocalDateTime hasta = LocalDate.of(2026, 7, 3).atTime(LocalTime.MAX);

    Subasta subastaFuera = Subasta.builder()
        .autor(perfilConObjectId)
        .figuritaSubastada(figurita)
        .fechaInicio(LocalDateTime.of(2026, 6, 30, 10, 0))
        .fechaCierre(LocalDateTime.of(2026, 7, 10, 10, 0))
        .build();
    repositorioSubastas.guardar(subastaFuera);

    List<RankingUsuarioDto> resultado = repositorioRankings.topSubastadores(desde, hasta, 5);

    assertTrue(resultado.isEmpty(),
        "El ranking debería estar vacío si no hay subastas dentro del período filtrado");
  }

  @Test
  void topSubastadores_conSubastaDentroDelPeriodo_aparaceEnRanking() {
    LocalDateTime desde = LocalDate.of(2026, 7, 1).atStartOfDay();
    LocalDateTime hasta = LocalDate.of(2026, 7, 3).atTime(LocalTime.MAX);

    Subasta subastaAdentro = Subasta.builder()
        .autor(perfilConObjectId)
        .figuritaSubastada(figurita)
        .fechaInicio(LocalDateTime.of(2026, 7, 2, 10, 0))
        .fechaCierre(LocalDateTime.of(2026, 7, 10, 10, 0))
        .build();
    repositorioSubastas.guardar(subastaAdentro);

    List<RankingUsuarioDto> resultado = repositorioRankings.topSubastadores(desde, hasta, 5);

    assertEquals(1, resultado.size(),
        "Debe haber exactamente un subastador en el período");
    assertNotNull(resultado.get(0).getNombre(),
        "El nombre del subastador no debe ser null (lookup resuelto correctamente)");
  }

  @Test
  void topSubastadores_conSubastasDentroYFueraDePeriodo_retornaSoloPeriodo() {
    LocalDateTime desde = LocalDate.of(2026, 7, 1).atStartOfDay();
    LocalDateTime hasta = LocalDate.of(2026, 7, 3).atTime(LocalTime.MAX);

    Subasta dentro = Subasta.builder()
        .autor(perfilConObjectId)
        .figuritaSubastada(figurita)
        .fechaInicio(LocalDateTime.of(2026, 7, 2, 10, 0))
        .fechaCierre(LocalDateTime.of(2026, 7, 10, 10, 0))
        .build();
    repositorioSubastas.guardar(dentro);

    Subasta fuera = Subasta.builder()
        .autor(perfilConStringId)
        .figuritaSubastada(figurita)
        .fechaInicio(LocalDateTime.of(2026, 6, 30, 10, 0))
        .fechaCierre(LocalDateTime.of(2026, 7, 10, 10, 0))
        .build();
    repositorioSubastas.guardar(fuera);

    List<RankingUsuarioDto> resultado = repositorioRankings.topSubastadores(desde, hasta, 5);

    assertEquals(1, resultado.size(),
        "Solo la subasta dentro del período debe aparecer");
    assertEquals(perfilConObjectId.getNombre(), resultado.get(0).getNombre(),
        "El subastador del período debe ser el perfil con id ObjectId");
  }

  @Test
  void topSubastadores_conSubastaConFechaActual_aparaceEnRankingDelDiaActual() {
    LocalDateTime ahora = LocalDateTime.now();
    LocalDateTime desde = ahora.minusHours(1);
    LocalDateTime hasta = ahora.plusHours(1);

    Subasta subastaDelSeed = Subasta.builder()
        .autor(perfilConObjectId)
        .figuritaSubastada(figurita)
        .fechaInicio(ahora)
        .fechaCierre(ahora.plusDays(7))
        .build();
    repositorioSubastas.guardar(subastaDelSeed);

    List<RankingUsuarioDto> resultado = repositorioRankings.topSubastadores(desde, hasta, 5);

    assertEquals(1, resultado.size(),
        "La subasta con fechaInicio=now() aparece en el ranking: esto es el efecto seed");
  }
}
