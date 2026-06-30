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

  /**
   * Busca figuritas intercambiables de forma paginada aplicando los filtros
   * indicados (número, selección, jugador y métodos de intercambio).
   *
   * @param numero        número de la figurita a buscar (opcional)
   * @param seleccion     selección o equipo de la figurita (opcional)
   * @param jugador       nombre del jugador de la figurita (opcional)
   * @param tipos         lista de métodos de intercambio por los que filtrar (opcional)
   * @param pagina        número de página solicitado (base 0)
   * @param tamanioPagina cantidad máxima de resultados por página
   * @return página de resultados con las figuritas intercambiables encontradas
   */
  public PaginaResultado<FiguritaIntercambiableDto> buscarFiguritas(
      Integer numero, String seleccion, String jugador,
      List<MetodoIntercambio> tipos, int pagina, int tamanioPagina) {

    FiguritasFiltro filtros = new FiguritasFiltro(null, numero, seleccion, jugador, tipos, null, null);
    PaginaResultado<FiguritaIntercambiableConPerfil> paginaRepo =
        repositorioColecciones.buscarIntercambiablesConFiltros(filtros, pagina, tamanioPagina);

    return mapearADto(paginaRepo);
  }
  /**
   * Busca figuritas intercambiables mediante una consulta de texto libre,
   * aplicando paginación y filtro opcional por métodos de intercambio.
   *
   * @param q             texto de búsqueda (nombre de jugador, selección, etc.)
   * @param tipos         lista de métodos de intercambio por los que filtrar (opcional)
   * @param pagina        número de página solicitado (base 0)
   * @param tamanioPagina cantidad máxima de resultados por página
   * @return página de resultados con las figuritas intercambiables que coinciden con la consulta
   */
  public PaginaResultado<FiguritaIntercambiableDto> buscarPorQuery(
      String q, List<MetodoIntercambio> tipos, int pagina, int tamanioPagina) {

    PaginaResultado<FiguritaIntercambiableConPerfil> paginaRepo =
        repositorioColecciones.buscarIntercambiablesPorQuery(q, tipos, pagina, tamanioPagina);

    return mapearADto(paginaRepo);
  }

  /**
   * Construye un mapa que asocia el identificador de cada figurita intercambiable
   * con el identificador de la subasta activa correspondiente, si existe.
   * Solo se consideran las figuritas que tengan el método {@link MetodoIntercambio#SUBASTA}.
   *
   * @param figuritas lista de figuritas intercambiables a evaluar
   * @return mapa de {@code idFigurita -> idSubasta} para aquellas figuritas con subasta activa
   */
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

  /**
   * Convierte una página de proyecciones {@link FiguritaIntercambiableConPerfil}
   * en una página de {@link FiguritaIntercambiableDto}, enriqueciendo cada DTO
   * con el identificador de la subasta activa cuando corresponde.
   *
   * @param paginaRepo página de proyecciones proveniente del repositorio
   * @return página de DTOs listos para la capa de presentación
   */
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

  /**
   * Busca figuritas base (no intercambiables) aplicando los filtros indicados.
   * Los resultados se devuelven como una lista plana de {@link FiguritaDto}.
   *
   * @param filtros criterios de filtrado (número, selección, jugador, etc.)
   * @return lista de figuritas base que coinciden con los filtros
   */
  public List<FiguritaDto> buscarFiguritasBase(
      FiguritasFiltro filtros
  ) {

    List<Figurita> resultado = this.repositorioFiguritas.buscarConFiltros(filtros);
    return resultado.stream()
        .map(FiguritaDto::new)
        .toList();
  }

}
