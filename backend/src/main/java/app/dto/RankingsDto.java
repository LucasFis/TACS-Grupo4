package app.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RankingsDto {
  private List<RankingUsuarioDto> topCreadoresDePropuestas;
  private List<RankingUsuarioDto> topIntercambiadores;
  private List<RankingUsuarioDto> mejorTasaAceptacion;
  private List<RankingUsuarioDto> topSubastadores;
  private List<RankingUsuarioDto> mejorReputacion;
  private List<RankingUsuarioDto> topColeccionistas;
}
