package app.dto.filtros;

public record SubastasFiltro(
  Integer pagina,
  Integer limite,
  String autorId,
  String participanteId,
  String estado
) {
  public SubastasFiltro {
    if(limite == null){
      limite = 10;
    }
    if(pagina == null){
      pagina = 1;
    }
  }
}
