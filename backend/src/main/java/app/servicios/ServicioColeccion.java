package app.servicios;

import app.dto.FiguritaDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.paginacion.PaginaResultado;
import app.dto.paginacion.Repetidas;
import app.dto.request.EditarRepetidaRequest;
import app.exceptions.BadRequestException;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.dto.filtros.FaltantesFiltro;
import app.dto.filtros.RepetidasFiltro;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;

import java.util.HashSet;
import java.util.List;

import app.repositories.impl.campos.CamposPerfil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioColeccion {
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioColecciones repositorioColecciones;
  private final RepositorioPerfiles repositorioPerfiles;
  private final ServicioNotificacion notificacionService;

  /**
   * Agrega una figurita a la lista de faltantes de una colección.
   *
   * @param colId identificador de la colección a la que se le agregará la figurita faltante
   * @param figId identificador de la figurita que se marcará como faltante
   * @throws app.exceptions.NotFoundException si la figurita con el {@code figId} indicado no existe
   * @throws app.exceptions.BadRequestException si la figurita ya está listada como faltante
   */
  public void agregarFaltante(String colId, String figId) {
    Figurita faltante = this.repositorioFiguritas.buscarPorId(figId);

    repositorioColecciones.agregarFaltante(colId, faltante);
  }

  /**
   * Agrega una figurita repetida a la colección indicada, marcándola como disponible
   * para intercambio, y notifica a los perfiles que tienen dicha figurita como faltante.
   *
   * @param colId identificador de la colección a la que se le agregará la figurita repetida
   * @param perfilId identificador del perfil propietario de la figurita repetida
   * @param figId identificador de la figurita que se agregará como repetida
   * @param cantidadExistente cantidad de unidades disponibles de la figurita
   * @param modosIntercambio lista de métodos de intercambio aceptados para la figurita
   * @throws app.exceptions.NotFoundException si la colección, el perfil o la figurita indicados no existen
   */
  @Transactional
  public void agregarRepetida(String colId, String perfilId, String figId, Integer
      cantidadExistente, List<MetodoIntercambio> modosIntercambio) {

    Figurita figurita = this.repositorioFiguritas.buscarPorId(figId);

    FiguritaIntercambiable repetida = new FiguritaIntercambiable(
        figurita, cantidadExistente, modosIntercambio, perfilId);

    this.repositorioColecciones.agregarRepetida(colId, repetida);

    List<Perfil> interesados = this.repositorioPerfiles.buscarPorFiguritaFaltante(figurita, new CamposPerfil(true));

    String cuerpo = "Nueva figurita disponible, Numero: " + figurita.getId() +
        ", Cantidad: " + cantidadExistente;

    this.notificacionService.notificarInteresados(interesados, cuerpo);
  }

  /**
   * Busca, de forma paginada, las figuritas faltantes de una colección según los filtros indicados.
   *
   * @param colId identificador de la colección sobre la que se buscarán las figuritas faltantes
   * @param filtros criterios de filtrado y paginación a aplicar en la búsqueda
   * @return página de resultados con las figuritas faltantes encontradas, representadas como {@link FiguritaDto}
   * @throws app.exceptions.NotFoundException si la colección con el {@code colId} indicado no existe
   */
  public PaginaResultado<FiguritaDto> buscarFaltantes(String colId, FaltantesFiltro filtros) {
    PaginaResultado<Figurita> resultado = this.repositorioColecciones.buscarFaltantes(colId, filtros);

    return new PaginaResultado<>(
        resultado.contenido().stream().map(FiguritaDto::new).toList(),
        resultado.cantidadDeElementos(),
        resultado.cantidadDePaginas(),
        resultado.numero());
  }

  /**
   * Busca, de forma paginada, las figuritas repetidas de una colección según los filtros indicados.
   * Si el filtro incluye un perfil, se utiliza la colección de faltantes de dicho perfil para
   * calcular la información de coincidencias (publicadas/disponibles).
   *
   * @param colId identificador de la colección sobre la que se buscarán las figuritas repetidas
   * @param filtros criterios de filtrado y paginación a aplicar en la búsqueda
   * @return resultado con la cantidad de figuritas publicadas, disponibles y la página de
   *         figuritas repetidas encontradas, representadas como {@link FiguritaIntercambiableDto}
   * @throws app.exceptions.NotFoundException si la colección con el {@code colId} indicado no existe
   */
  public Repetidas<FiguritaIntercambiableDto> buscarRepetidas(String colId, RepetidasFiltro filtros) {
    String colIdFaltantes = resolverColIdFaltantes(filtros.perfilId());

    Repetidas<FiguritaIntercambiable> repetidas =
        this.repositorioColecciones.buscarRepetidas(colId, filtros, colIdFaltantes);

    PaginaResultado<FiguritaIntercambiableDto> paginacionDto =
        repetidas.getData().mapearA(FiguritaIntercambiableDto::new);

    return new Repetidas<>(repetidas.getPublicadas(), repetidas.getDisponibles(), paginacionDto);
  }

  @Transactional
  public void editarRepetida(String colId, String figId, EditarRepetidaRequest req) {
    FiguritaIntercambiable repetidaAmodificar = this.repositorioColecciones.buscarRepetida(colId, figId);

    if(req.cantidadNueva() < repetidaAmodificar.getCantidadReservada()) {
      throw new BadRequestException("No se puede tener menos cantidad que las reservadas");
    }

    repetidaAmodificar.setCantidadExistente(req.cantidadNueva());

    if (!req.metodos().isEmpty()) {
      validarQueNoQuiteMetodos(
          repetidaAmodificar.getMetodos(),
          req.metodos()
      );

      repetidaAmodificar.setMetodos(req.metodos());
    }

    this.repositorioColecciones.actualizarRepetida(colId, figId, repetidaAmodificar);
  }

  /**
   * Resuelve el identificador de la colección de faltantes asociada a un perfil.
   *
   * @param perfilId identificador del perfil del cual se desea obtener la colección de faltantes,
   *                  o {@code null} si no se desea resolver ninguna colección
   * @return el identificador de la colección de faltantes del perfil, o {@code null} si
   *         {@code perfilId} es {@code null}
   */
  private String resolverColIdFaltantes(String perfilId) {
    if (perfilId == null) return null;
    Perfil perfilFaltantes = this.repositorioPerfiles.buscarPorId(perfilId, new CamposPerfil(false));
    return perfilFaltantes.getColeccion().getId();
  }

  private void validarQueNoQuiteMetodos(
      List<MetodoIntercambio> actuales,
      List<MetodoIntercambio> nuevos
  ) {
    if (!new HashSet<>(nuevos).containsAll(actuales)) {
      throw new BadRequestException(
          "No se pueden eliminar métodos de intercambio existentes"
      );
    }
  }
}

