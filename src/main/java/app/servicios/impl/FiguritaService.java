package app.servicios.impl;

import app.dto.FiguritaIntercambiableDto;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Figurita;
import app.model.entities.Seleccion;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.repositories.RepositorioFiguritas;
import app.servicios.IFiguritaService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FiguritaService implements IFiguritaService {

  private final RepositorioFiguritasIntercambiables repositorioIntercambiables;
  private final RepositorioFiguritas repositorioFiguritas;

  public FiguritaService(RepositorioFiguritasIntercambiables repositorioIntercambiables,
                         RepositorioFiguritas repositorioFiguritas) {
    this.repositorioIntercambiables = repositorioIntercambiables;
    this.repositorioFiguritas = repositorioFiguritas;
  }

  public List<FiguritaIntercambiableDto> buscarFiguritas(
      Integer numero, Seleccion seleccion, String jugador) {

    List<String> idsFiltrados = repositorioFiguritas
        .buscarConFiltros(numero, seleccion, jugador)
        .stream()
        .map(Figurita::getId)
        .toList();

    return repositorioIntercambiables
        .buscarPorFiguritaIds(idsFiltrados)
        .stream()
        .map(this::aDto)
        .toList();
  }

  private FiguritaIntercambiableDto aDto(FiguritaIntercambiable fi) {
    return new FiguritaIntercambiableDto(
        fi.getFigurita().getId(),
        fi.getFigurita().getNumero(),
        fi.getFigurita().getJugador(),
        fi.getFigurita().getSeleccion(),
        fi.getCantidadDisponible(),
        fi.getMetodos(),
        fi.getUsuarioId()
    );
  }
}