package app.servicios;

import app.dto.FiguritaDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.model.entities.Subasta;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.dto.filtros.FiguritasFiltro;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioSubastas;
import java.util.List;

import app.repositories.impl.campos.CamposPerfil;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioFigurita {

  private final RepositorioColecciones repositorioColecciones;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioSubastas repositorioSubastas;

  public PaginaResultado<FiguritaIntercambiableDto> buscarFiguritas(
      Integer numero, String seleccion, String jugador,
      List<MetodoIntercambio> tipos, int pagina, int tamanioPagina) {

    FiguritasFiltro filtros = new FiguritasFiltro(null, numero, seleccion, jugador, tipos);
    PaginaResultado<FiguritaIntercambiable> paginaRepo =
        repositorioColecciones.buscarIntercambiablesConFiltros(filtros, pagina, tamanioPagina);

    return mapearADto(paginaRepo);
  }
  public PaginaResultado<FiguritaIntercambiableDto> buscarPorQuery(
      String q, List<MetodoIntercambio> tipos, int pagina, int tamanioPagina) {

    PaginaResultado<FiguritaIntercambiable> paginaRepo =
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
      PaginaResultado<FiguritaIntercambiable> paginaRepo) {

    Map<String, String> figuritaIdASubastaId = obtenerMapaSubastasActivas(paginaRepo.contenido());

    List<FiguritaIntercambiableDto> contenido = paginaRepo.contenido().stream()
        .map(fi -> new FiguritaIntercambiableDto(
            fi,
            buscarPerfil(fi.getPerfilId()),
            figuritaIdASubastaId.get(fi.getFigurita().getId())
        ))
        .toList();

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

  private Perfil buscarPerfil(String perfilId) {
    try {
      return repositorioPerfiles.buscarPorId(perfilId, new CamposPerfil(false));
    } catch (NotFoundException e) {
      return null;
    }
  }
}
