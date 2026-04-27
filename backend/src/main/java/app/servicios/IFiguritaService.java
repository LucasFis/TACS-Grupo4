package app.servicios;

import app.dto.FiguritaIntercambiableDto;
import app.model.entities.Seleccion;
import java.util.List;

public interface IFiguritaService {
  public List<FiguritaIntercambiableDto> buscarFiguritas (Integer numero, Seleccion seleccion,
                                                          String jugador);
}
