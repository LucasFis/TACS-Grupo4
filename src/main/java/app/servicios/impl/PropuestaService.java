package app.servicios.impl;

import app.dto.PropuestaDto;
import app.dto.request.CrearPropuestaRequest;
import app.exceptions.NotFoundException;
import app.model.entities.EstadoProceso;
import app.model.entities.Figurita;
import app.model.entities.FiguritaIntercambiable;
import app.model.entities.Propuesta;
import app.model.entities.Usuario;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioFiguritasIntercambiables;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioUsuarios;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class PropuestaService {

  private final RepositorioPropuestas repositorioPropuestas;
  private final RepositorioUsuarios repositorioUsuarios;
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioFiguritasIntercambiables repositorioIntercambiables;

  public PropuestaService(RepositorioPropuestas repositorioPropuestas,
                          RepositorioUsuarios repositorioUsuarios,
                          RepositorioFiguritas repositorioFiguritas,
                          RepositorioFiguritasIntercambiables repositorioIntercambiables) {
    this.repositorioPropuestas = repositorioPropuestas;
    this.repositorioUsuarios = repositorioUsuarios;
    this.repositorioFiguritas = repositorioFiguritas;
    this.repositorioIntercambiables = repositorioIntercambiables;
  }

  /**
   * Crea una propuesta de intercambio. Valida que el usuario origen,
   * destino y figuritas existan. El estado inicial es PENDIENTE.
   */
  public PropuestaDto crearPropuesta(CrearPropuestaRequest request) {
    Usuario origen  = repositorioUsuarios.findById(request.getUsuarioOrigenId());
    Usuario destino = repositorioUsuarios.findById(request.getUsuarioDestinoId());

    if (origen  == null) throw new NotFoundException("Usuario origen no encontrado");
    if (destino == null) throw new NotFoundException("Usuario destino no encontrado");

    Figurita figuritaBuscada = repositorioFiguritas
        .findById(request.getFiguritaBuscadaId());

    List<Figurita> figuritasOfrecidas = request.getFiguritasOfrecedasIds()
        .stream()
        .map(repositorioFiguritas::findById)
        .toList();

    Propuesta propuesta = new Propuesta(
        UUID.randomUUID().toString(),
        origen,
        destino,
        figuritasOfrecidas,
        figuritaBuscada,
        EstadoProceso.PENDIENTE
    );

    repositorioPropuestas.save(propuesta);

    return toDto(propuesta);
  }
  
//  public Propuesta obtenerPorId(String id) {
//      Propuesta propuesta = this.repositorioPropuestas.findById(id);
//
//      if (propuesta == null) {
//          throw new RuntimeException("Propuesta no encontrada");
//      }
//
//      return propuesta;
//  }

//  public void aceptar(String id) {
//      Propuesta propuesta = obtenerPorId(id);
//      propuesta.aceptar(propuesta.getUsuarioDestino());
//  }
//
//  public void rechazar(String id) {
//      Propuesta propuesta = obtenerPorId(id);
//      propuesta.rechazar(propuesta.getUsuarioDestino());
//  }

  private PropuestaDto toDto(Propuesta p) {
    return new PropuestaDto(
        p.getId(),
        p.getUsuarioOrigen().getId(),
        p.getUsuarioDestino().getId(),
        p.getFiguritaBuscada().getId(),
        p.getFiguritasOfrecidas().stream().map(Figurita::getId).toList(),
        p.getEstado()
    );
  }
}