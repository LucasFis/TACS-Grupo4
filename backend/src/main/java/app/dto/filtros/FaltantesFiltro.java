package app.dto.filtros;

public record FaltantesFiltro (
    Integer limite,
    Integer pagina
) {
  public FaltantesFiltro {
    if(limite == null){
      limite = 10;
    }
    if(pagina == null){
      pagina = 1;
    }
  }
}
