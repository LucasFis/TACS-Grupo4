package app.servicios;

import app.dto.FiguritaDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.filtros.FiguritasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Subasta;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioSubastas;
import app.repositories.projections.FiguritaIntercambiableConPerfil;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioFigurita {

  private final RepositorioColecciones repositorioColecciones;
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioSubastas repositorioSubastas;
  private final MeterRegistry meterRegistry;

  public PaginaResultado<FiguritaIntercambiableDto> buscarFiguritas(
      Integer numero, String seleccion, String jugador,
      List<MetodoIntercambio> tipos, int pagina, int tamanioPagina) {

    FiguritasFiltro filtros = new FiguritasFiltro(null, numero, seleccion, jugador, tipos);
    PaginaResultado<FiguritaIntercambiableConPerfil> paginaRepo =
        repositorioColecciones.buscarIntercambiablesConFiltros(filtros, pagina, tamanioPagina);

    return mapearADto(paginaRepo);
  }
  public PaginaResultado<FiguritaIntercambiableDto> buscarPorQuery(
      String q, List<MetodoIntercambio> tipos, int pagina, int tamanioPagina) {

    PaginaResultado<FiguritaIntercambiableConPerfil> paginaRepo =
        repositorioColecciones.buscarIntercambiablesPorQuery(q, tipos, pagina, tamanioPagina);

    return mapearADto(paginaRepo);
  }

  private Map<String, String> obtenerMapaSubastasActivas(List<FiguritaIntercambiable> figuritas) {
    List<String> figuritaIds = figuritas.stream()
        .filter(fi -> fi.getMetodos().contains(MetodoIntercambio.SUBASTA))
        .map(fi -> fi.getFigurita().getId())
        .toList();

    if (figuritaIds.isEmpty()) return Map.of();

    return repositorioSubastas.buscarActivasPorFiguritasSubastadas(figuritaIds)
        .stream()
        .collect(Collectors.toMap(
            s -> s.getFiguritaSubastada().getId(),
            Subasta::getId
        ));
  }

  private PaginaResultado<FiguritaIntercambiableDto> mapearADto(
      PaginaResultado<FiguritaIntercambiableConPerfil> paginaRepo) {

    List<FiguritaIntercambiable> figuritas = paginaRepo.contenido().stream()
        .map(FiguritaIntercambiableConPerfil::figurita)
        .toList();
    Map<String, String> figuritaIdASubastaId = obtenerMapaSubastasActivas(figuritas);

    List<FiguritaIntercambiableDto> contenido = paginaRepo.contenido().stream()
        .map(resultado -> new FiguritaIntercambiableDto(
            resultado.figurita(),
            resultado.perfil(),
            figuritaIdASubastaId.get(resultado.figurita().getFigurita().getId())
        ))
        .toList();

    // Cuántos matches devuelve cada consulta de intercambiables. Una distribución con muchos
    // ceros señala matching/inventario pobre que el HTTP 200 oculta. Sin tags: cardinalidad nula.
    meterRegistry.summary("figuritas_intercambiables_matches").record(paginaRepo.cantidadDeElementos());

    return new PaginaResultado<>(contenido, paginaRepo.cantidadDeElementos(),
        paginaRepo.cantidadDePaginas(), paginaRepo.numero());
  }

  public List<FiguritaDto> buscarFiguritasBase(
      FiguritasFiltro filtros
  ) {

    List<Figurita> resultado = this.repositorioFiguritas.buscarConFiltros(filtros);
    return resultado.stream()
        .map(FiguritaDto::new)
        .toList();
  }

}
