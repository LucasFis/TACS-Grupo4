package app.dto.filtros;

public record FaltantesFiltro (
    Integer limite,
    Integer pagina,
    String jugador
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
