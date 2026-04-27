package app.servicios.impl;

import app.dto.SubastaDto;
import app.exceptions.BadRequestException;
import app.model.entities.EstadoProceso;
import app.model.entities.Figurita;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.model.entities.Perfil;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioPerfiles;
import app.servicios.INotificacionService;
import app.servicios.ISubastaService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubastaServiceImpl implements ISubastaService {
  private final RepositorioSubastas repoSubasta;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioFiguritas repoFigurita;
  private final RepositorioPropuestas repoPropuesta;
  private final INotificacionService notificacionService;

  public SubastaServiceImpl(RepositorioSubastas repoSubasta, RepositorioPerfiles repoUsuario,
                            RepositorioFiguritas repoFigurita, RepositorioPropuestas repoPropuesta,
                            INotificacionService notificacionService) {
    this.repoSubasta = repoSubasta;
    this.repositorioPerfiles = repoUsuario;
    this.repoFigurita = repoFigurita;
    this.repoPropuesta = repoPropuesta;
    this.notificacionService = notificacionService;
  }

  @Override
  public SubastaDto crearSubasta(String userId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                 String figuritaId) {
    Perfil perfil = this.repositorioPerfiles.buscarPorUsuarioId(userId);
    Figurita figuritaSubastada = this.repoFigurita.buscarPorId(figuritaId);

    Subasta nuevaSubasta = new Subasta(
        UUID.randomUUID().toString(),
        perfil,
        fechaInicio,
        fechaFin,
        figuritaSubastada
    );

    this.repoSubasta.guardar(nuevaSubasta);

    List<Perfil> interesados = this.repositorioPerfiles
        .buscarPorFiguritaFaltante(nuevaSubasta.getFiguritaSubastada());

    this.notificacionService.notificarInteresados(
        interesados, "Encontramos una subasta de una figurita que te falta!");

    return new SubastaDto(nuevaSubasta);
  }

  @Override
  public SubastaDto ofertarEnSubasta(String userId, String perfilDestinoId,
                                     String subastaId, List<String> rawFiguritasId) {
    Perfil autor        = this.repositorioPerfiles.buscarPorUsuarioId(userId);
    Perfil destinatario = this.repositorioPerfiles.buscarPorId(perfilDestinoId);
    Subasta subasta     = this.repoSubasta.buscarPorId(subastaId);

    Figurita figuritaBuscada = subasta.getFiguritaSubastada();

    if (rawFiguritasId.size() != rawFiguritasId.stream().distinct().count()) {
      throw new BadRequestException("Figuritas ofrecidas repetidas");
    }

    List<Figurita> figuritasOfrecidas = rawFiguritasId.stream()
        .map(this.repoFigurita::buscarPorId)
        .toList();

    Propuesta nuevaPropuesta = new Propuesta(
        UUID.randomUUID().toString(),
        autor,
        destinatario,
        figuritasOfrecidas,
        figuritaBuscada
    );

    subasta.getOfertas().add(nuevaPropuesta);
    this.repoSubasta.guardar(subasta);

    return new SubastaDto(subasta);
  }
}
