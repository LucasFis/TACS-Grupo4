package app.repositories;

import app.dto.filtros.FiguritasFiltro;
import app.model.entities.Figurita;
import java.time.Duration;
import java.util.List;

public interface RepositorioFiguritas {

  /**
   * Busca una figurita por su identificador.
   *
   * @param id identificador de la figurita
   * @return la figurita encontrada
   * @throws app.exceptions.NotFoundException si no existe figurita con ese id
   */
  Figurita buscarPorId(String id);

  /**
   * Busca múltiples figuritas por sus identificadores.
   *
   * @param ids lista de identificadores
   * @return lista de figuritas encontradas
   */
  List<Figurita> buscarPorIds(List<String> ids);

  /**
   * Retorna las figuritas que cumplan los filtros provistos, paginadas segun
   * {@link FiguritasFiltro#paginaEfectiva()} y {@link FiguritasFiltro#tamanioPaginaEfectivo()}.
   * Los parámetros nulos se ignoran. {@code jugador} usa contains case-insensitive.
   *
   * @throws app.exceptions.NotFoundException si ninguna figurita coincide con los filtros
   */
  List<Figurita> buscarConFiltros(FiguritasFiltro filtros);

  void guardar(Figurita figurita);

  /**
   * Retorna hasta {@code tamanioPagina} figuritas pendientes: {@code imagenStatus} null,
   * {@code EN_PROCESO} con {@code imagenCreadoEn} anterior a {@code now - ttlProcesamiento}
   * (registros colgados), o {@code COMPLETADO} con {@code imagenCreadoEn} anterior a
   * {@code now - ttlRefresco} (imagen vencida, a refrescar).
   */
  List<Figurita> buscarPendientes(Duration ttlProcesamiento, Duration ttlRefresco, int tamanioPagina);

  /**
   * Intenta atómicamente reclamar el procesamiento de una figurita via {@code findAndModify}.
   * La query sólo matchea si el documento está en estado pendiente (ver {@link #buscarPendientes}).
   * Al matchear actualiza {@code imagenStatus=EN_PROCESO} e {@code imagenCreadoEn=now}, por lo que
   * un segundo llamador concurrente ya no matchea el TTL expirado.
   *
   * @return el documento <em>anterior</em> si el claim fue exitoso, {@code null} si otro proceso
   *     ya lo tomó
   */
  Figurita reclamarParaProcesamiento(String figuritaId, Duration ttlProcesamiento, Duration ttlRefresco);
}
