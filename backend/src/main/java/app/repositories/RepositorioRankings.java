package app.repositories;

import app.dto.RankingUsuarioDto;
import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioRankings {

  /**
   * Usuarios que más propuestas crearon en el período (como autor).
   */
  List<RankingUsuarioDto> topCreadoresDePropuestas(LocalDateTime desde, LocalDateTime hasta, int limite);

  /**
   * Usuarios con más intercambios concretados (propuestas aceptadas) en el período,
   * contando tanto al autor como al destinatario de cada intercambio.
   */
  List<RankingUsuarioDto> topIntercambiadores(LocalDateTime desde, LocalDateTime hasta, int limite);

  /**
   * Usuarios con mejor tasa de aceptación sobre las propuestas que recibieron en el período.
   * Solo considera usuarios con al menos {@code minimo} propuestas recibidas.
   */
  List<RankingUsuarioDto> mejorTasaAceptacion(LocalDateTime desde, LocalDateTime hasta, int minimo, int limite);

  /**
   * Usuarios que más subastas crearon en el período.
   */
  List<RankingUsuarioDto> topSubastadores(LocalDateTime desde, LocalDateTime hasta, int limite);

  /**
   * Usuarios con mejor calificación media. Solo considera perfiles con al menos
   * {@code minimoCalificaciones} calificaciones recibidas.
   */
  List<RankingUsuarioDto> mejorReputacion(int minimoCalificaciones, int limite);

  /**
   * Usuarios con más figuritas repetidas publicadas en su colección.
   */
  List<RankingUsuarioDto> topColeccionistas(int limite);
}
