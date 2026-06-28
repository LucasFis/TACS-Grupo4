package app.repositories;

import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.Perfil;
import app.model.entities.Sugerencia;

import java.util.List;

public interface RepositorioSugerencias {
  void guardar(Sugerencia sugerencia);

  void guardar(List<Sugerencia> sugerencias);

  PaginaResultado<Sugerencia> buscarPorPerfil(Perfil perfil, SugerenciasFiltro filtro);

  /**
   * Genera sugerencias de intercambio para una colección objetivo, cruzando faltantes
   * y repetidos con otras colecciones.
   *
   * @param perfil perfil autor al cual generar sugerencias
   * @return página de sugerencias de intercambio
   */
  List<Sugerencia> generarSugerencias(Perfil perfil);

  void alternarFavorito(String sugerenciaId, String perfilId);

  Sugerencia buscarPorId(String id);

  void eliminacionProgramada();
}
