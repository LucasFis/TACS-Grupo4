package app.repositories;

import app.dto.paginacion.PaginaResultado;
import app.dto.paginacion.Repetidas;
import app.model.entities.Coleccion;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.repositories.projections.FiguritaIntercambiableConPerfil;
import app.dto.filtros.FaltantesFiltro;
import app.dto.filtros.FiguritasFiltro;
import app.dto.filtros.RepetidasFiltro;
import app.repositories.impl.campos.CamposColeccion;

import java.util.List;

public interface RepositorioColecciones {

  /**
   * Busca una colección por su identificador, cargando selectivamente los campos
   * indicados para evitar lecturas innecesarias.
   *
   * @param colId  identificador de la colección
   * @param campos especifica qué campos incluir (repetidas, faltantes)
   * @return la colección encontrada
   * @throws app.exceptions.NotFoundException si no existe la colección
   */
  Coleccion buscarPorId(String colId, CamposColeccion campos);

  /**
   * Persiste una colección completa.
   *
   * @param coleccion la colección a guardar
   */
  void guardar(Coleccion coleccion);

  /**
   * Actualiza selectivamente los campos de una colección según los flags
   * de {@link CamposColeccion}.
   *
   * @param coleccion colección con los datos a persistir
   * @param campos    especifica qué campos actualizar
   */
  void guardar(Coleccion coleccion, CamposColeccion campos);

  /**
   * Agrega una figurita a la lista de faltantes de una colección si no está ya presente.
   *
   * @param colId identificador de la colección
   * @param figId figurita a marcar como faltante
   * @throws app.exceptions.BadRequestException si la figurita ya está listada como faltante
   */
  void agregarFaltante(String colId, Figurita figId);

  /**
   * Agrega una figurita repetida a la colección. Si ya existe, incrementa su
   * cantidad disponible; si no, la agrega como nuevo elemento.
   *
   * @param colId identificador de la colección
   * @param figId figurita intercambiable a agregar
   */
  void agregarRepetida(String colId, FiguritaIntercambiable figId);

  /**
   * Busca figuritas repetidas de una colección con filtros y paginación.
   * Si se proporciona {@code colIdFaltantes}, cruza con los faltantes de
   * ese perfil para mostrar solo figuritas que le interesen.
   *
   * @param colId          identificador de la colección
   * @param filtros        criterios de filtrado (método de intercambio, paginación)
   * @param colIdFaltantes identificador de la colección de faltantes para cruce, o {@code null}
   * @return resultado con las repetidas encontradas, total publicadas y disponibles
   */
  Repetidas<FiguritaIntercambiable> buscarRepetidas(String colId, RepetidasFiltro filtros, String colIdFaltantes);

  /**
   * Busca las figuritas faltantes de una colección con paginación, realizando
   * {@code $lookup} contra la colección de figuritas para obtener los datos completos.
   *
   * @param colId   identificador de la colección
   * @param filtros criterios de paginación
   * @return página de figuritas faltantes con sus datos completos
   */
  PaginaResultado<Figurita> buscarFaltantes(String colId, FaltantesFiltro filtros);

  /**
   * Busca figuritas intercambiables en todas las colecciones aplicando filtros
   * (número, selección, jugador, métodos de intercambio). Incluye datos del perfil
   * propietario.
   *
   * @param filtros       criterios de filtrado
   * @param pagina        número de página (base 1)
   * @param tamanioPagina cantidad máxima de resultados por página
   * @return página de figuritas intercambiables con datos del perfil asociado
   */
  PaginaResultado<FiguritaIntercambiableConPerfil> buscarIntercambiablesConFiltros(
      FiguritasFiltro filtros, int pagina, int tamanioPagina);

  /**
   * Busca por texto libre: cada término (separado por espacios) debe matchear
   * jugador, selección o número en OR. Entre términos se aplica AND.
   * El filtro {@code tipos} se aplica en AND sobre el resultado.
   */
  PaginaResultado<FiguritaIntercambiableConPerfil> buscarIntercambiablesPorQuery(
      String q, List<MetodoIntercambio> tipos, int pagina, int tamanioPagina);

  /**
   * Busca figuritas intercambiables cuyos IDs de figurita base estén en la lista proporcionada.
   *
   * @param figuritaIds lista de IDs de figuritas base a buscar
   * @return lista de figuritas intercambiables encontradas
   */
  List<FiguritaIntercambiable> buscarIntercambiablesPorFiguritaIds(List<String> figuritaIds);

  /**
   * Busca todas las figuritas intercambiables de un perfil.
   *
   * @param usuarioId identificador del perfil
   * @return lista de figuritas intercambiables del perfil
   */
  List<FiguritaIntercambiable> buscarIntercambiablesPorPerfilId(String usuarioId);

  FiguritaIntercambiable buscarRepetida(String colId, String figId);
  void actualizarRepetida( String colId,
                        String figId,
                        FiguritaIntercambiable repetida);

  /**
   * Cuenta la cantidad total de figuritas repetidas en todas las colecciones,
   * opcionalmente filtradas por métodos de intercambio.
   *
   * @param filtros lista de métodos de intercambio para filtrar (vacía = sin filtro)
   * @return total de ejemplares repetidos que cumplen los criterios
   */
  long contarRepetidas(List<MetodoIntercambio> filtros);
}
