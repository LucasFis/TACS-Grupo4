package app.dto.filtros;

public record SugerenciasFiltro (
    Integer pagina,
    Integer limite) {
  public SugerenciasFiltro {
    if(limite == null){
      limite = 10;
    }
    if(pagina == null){
      pagina = 1;
    }
  }
}
