package app.servicios.impl;

import app.dto.TemporalDto;
import app.model.entities.EstadoProceso;
import app.model.entities.Figurita;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioUsuarios;
import app.servicios.SubastaService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SubastaServiceImpl implements SubastaService {
  private RepositorioSubastas repoSubasta;
  private RepositorioUsuarios repoUsuario;
  private RepositorioFiguritas repoFigurita;
  private RepositorioPropuestas repoPropuesta;

  public SubastaServiceImpl(RepositorioSubastas repoSubasta, RepositorioUsuarios repoUsuario, RepositorioFiguritas repoFigurita, RepositorioPropuestas repoPropuesta) {
    this.repoSubasta = repoSubasta;
    this.repoUsuario = repoUsuario;
    this.repoFigurita = repoFigurita;
    this.repoPropuesta = repoPropuesta;
  }

  @Override
  public Subasta crearSubasta(String userId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                              String figuritaId, Propuesta propuestaGanadora) {
    Usuario usuario = this.repoUsuario.findById(userId);
    Figurita figuritaSubastada = this.repoFigurita.findById(figuritaId);
    Subasta nuevaSubasta = new Subasta(
        usuario, fechaInicio, fechaFin,
        figuritaSubastada, propuestaGanadora);

    this.repoSubasta.save(nuevaSubasta);
    return nuevaSubasta;
  }

  @Override
  public boolean ofertarEnSubasta(String userId, String usuarioDestinoId,
                               String subastaId, List<Object> rawFiguritasId) {
    Usuario usuarioOrigen = this.repoUsuario.findById(userId);
    Usuario usuarioDestino = this.repoUsuario.findById(usuarioDestinoId);
    Subasta subasta = this.repoSubasta.findById(subastaId);
    List<Figurita> figuritasOfrecidas = new ArrayList<>();

    Figurita figuritaBuscada = subasta.getFiguritaSubastada();

    if (rawFiguritasId.size() != rawFiguritasId.stream().distinct().count()) {
      //El listado debe tener figuritas distintas
      throw new RuntimeException("Figuritas ofrecidas repetidas");
    }

    rawFiguritasId.forEach(figuritaId -> {
      Figurita figurita = this.repoFigurita.findById((String) figuritaId);
      figuritasOfrecidas.add(figurita);
    });

    Propuesta nuevaPropuesta = new Propuesta(usuarioOrigen, usuarioDestino, figuritasOfrecidas, figuritaBuscada, EstadoProceso.PENDIENTE);

    this.repoPropuesta.save(nuevaPropuesta);

    subasta.algoritmoSeleccionador(nuevaPropuesta);

    this.repoSubasta.save(subasta);

    System.out.println("La subasta tiene " + subasta.getPropuestaGanadora() + " propuesta ganadora");

    //TODO: Aca verificar con id en persistencia real.
    return Objects.equals(nuevaPropuesta, subasta.getPropuestaGanadora());
  }
}
