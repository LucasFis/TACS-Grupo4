package app.servicios;

import app.dto.SugerenciaDto;
import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.Perfil;
import app.model.entities.Sugerencia;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioSugerencias;
import app.repositories.impl.campos.CamposPerfil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioSugerencia {
  private final RepositorioSugerencias repositorioSugerencias;
  private final RepositorioPerfiles repositorioPerfiles;

  /**
   * Genera sugerencias de intercambio para un perfil basándose en su colección
   * y los filtros de búsqueda proporcionados.
   *
   * @param perfilId identificador del perfil para el cual se generarán sugerencias
   * @return sugerenicas para el perfil enviado
   * @throws app.exceptions.NotFoundException si no se encuentra el perfil indicado
   */
  public PaginaResultado<SugerenciaDto> obtenerSugerencias(String perfilId, SugerenciasFiltro filtros) {
    CamposPerfil campos = new CamposPerfil(false);
    Perfil perfilObjetivo = this.repositorioPerfiles.buscarPorId(perfilId, campos);

    PaginaResultado<Sugerencia> sugerencias = this.repositorioSugerencias.buscarPorPerfil(perfilObjetivo, filtros);

    return new PaginaResultado<>(sugerencias.contenido().stream().map(SugerenciaDto::new).toList(),
        sugerencias.cantidadDeElementos(), sugerencias.cantidadDePaginas(), sugerencias.numero());
  }

  public void alternarFavorito(String sugerenciaId, String perfilId) {

    this.repositorioSugerencias.alternarFavorito(sugerenciaId, perfilId);
  }
}
