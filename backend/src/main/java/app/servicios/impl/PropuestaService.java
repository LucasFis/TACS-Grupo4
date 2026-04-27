package app.servicios.impl;

import app.dto.PropuestaDto;
import app.dto.request.CrearPropuestaRequest;
import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.model.entities.Propuesta;
import app.model.entities.Perfil;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioPerfiles;
import app.servicios.INotificacionService;
import app.servicios.IPropuestaService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class PropuestaService implements IPropuestaService {

  private final RepositorioPropuestas repositorioPropuestas;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioFiguritasIntercambiables repositorioIntercambiables;
  private final INotificacionService notificacionService;

  public PropuestaService(RepositorioPropuestas repositorioPropuestas,
                          RepositorioPerfiles repositorioUsuarios,
                          RepositorioFiguritas repositorioFiguritas,
                          RepositorioFiguritasIntercambiables repositorioIntercambiables,
                          INotificacionService notificacionService) {
    this.repositorioPropuestas = repositorioPropuestas;
    this.repositorioPerfiles = repositorioUsuarios;
    this.repositorioFiguritas = repositorioFiguritas;
    this.repositorioIntercambiables = repositorioIntercambiables;
    this.notificacionService = notificacionService;
  }

  /**
   * Crea una propuesta de intercambio. Valida que el usuario origen,
   * destino y figuritas existan. El estado inicial es PENDIENTE.
   */
  public PropuestaDto crearPropuesta(CrearPropuestaRequest request) {
    Perfil origen  = repositorioPerfiles.buscarPorId(request.getAutorId());
    Perfil destino = repositorioPerfiles.buscarPorId(request.getDestinatarioId());

    if (origen  == null) throw new NotFoundException("Usuario origen no encontrado");
    if (destino == null) throw new NotFoundException("Usuario destino no encontrado");

    Figurita figuritaBuscada = repositorioFiguritas
        .buscarPorId(request.getFiguritaBuscadaId());

    List<Figurita> figuritasOfrecidas = request.getFiguritasOfrecidasIds()
        .stream()
        .map(repositorioFiguritas::buscarPorId)
        .toList();

    Propuesta propuesta = new Propuesta(
        UUID.randomUUID().toString(),
        origen,
        destino,
        figuritasOfrecidas,
        figuritaBuscada
    );

    repositorioPropuestas.guardar(propuesta);

    String cuerpo = "Tenes una nueva propuesta de: " + origen.getNombre()
        + " por la figurita numero: " + figuritaBuscada.getNumero();

    notificacionService.notificarInteresados(List.of(destino), cuerpo);

    return new PropuestaDto(propuesta);
  }
  public void aceptar(String propuestaId, String usuarioId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(propuestaId);
    Perfil perfil = repositorioPerfiles.buscarPorUsuarioId(usuarioId);
    propuesta.aceptar(perfil);
    repositorioPropuestas.guardar(propuesta);
  }

  public void rechazar(String id, String usuarioId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(id);
    Perfil perfil = repositorioPerfiles.buscarPorUsuarioId(usuarioId);
    propuesta.rechazar(perfil);
    repositorioPropuestas.guardar(propuesta);
  }
}