package app.servicios;

import app.dto.PropuestaDto;
import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearPropuestaRequest;
import app.exceptions.NotFoundException;
import app.model.entities.Figurita;
import app.model.entities.Perfil;
import app.model.entities.Propuesta;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioPropuestas;

import java.util.ArrayList;
import java.util.List;

import app.repositories.impl.campos.CamposPerfil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioPropuesta {

  private final RepositorioPropuestas repositorioPropuestas;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioFiguritas repositorioFiguritas;
  private final ServicioNotificacion notificacionService;

  /**
   * Crea una propuesta de intercambio. Valida que el usuario origen,
   * destino y figuritas existan. El estado inicial es PENDIENTE.
   */
  @Transactional
  public PropuestaDto crearPropuesta(String autorId, CrearPropuestaRequest request) {
    CamposPerfil sinCampos = new CamposPerfil(false);
    Perfil origen = repositorioPerfiles.buscarPorId(autorId, sinCampos);
    Perfil destino = repositorioPerfiles.buscarPorId(request.getDestinatarioId(), sinCampos);

    if (origen == null) throw new NotFoundException("Usuario origen no encontrado");
    if (destino == null) throw new NotFoundException("Usuario destino no encontrado");

    Figurita figuritaBuscada = repositorioFiguritas
        .buscarPorId(request.getFiguritaBuscadaId());

    List<Figurita> figuritasOfrecidas = request.getFiguritasOfrecidasIds()
        .stream()
        .map(repositorioFiguritas::buscarPorId)
        .toList();

    Propuesta propuesta = Propuesta.builder()
        .autor(origen).destinatario(destino)
        .figuritaBuscada(figuritaBuscada)
        .figuritasOfrecidas(figuritasOfrecidas)
        .build();

    repositorioPropuestas.guardar(propuesta);

    String cuerpo = "Tenes una nueva propuesta de: " + origen.getNombre()
        + " por la figurita numero: " + figuritaBuscada.getNumero();

    notificacionService.notificarInteresados(List.of(destino), cuerpo);

    return new PropuestaDto(propuesta);
  }

  @Transactional
  public void aceptar(String propuestaId, String perfilId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(propuestaId);
    propuesta.aceptar(perfilId);
    repositorioPropuestas.guardar(propuesta);
  }

  @Transactional
  public void rechazar(String id, String perfilId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(id);
    propuesta.rechazar(perfilId);
    repositorioPropuestas.guardar(propuesta);
  }

  @Transactional
  public void cancelar(String id, String perfilId) {
    Propuesta propuesta = repositorioPropuestas.buscarPorId(id);
    propuesta.cancelar(perfilId);
    this.repositorioPropuestas.guardar(propuesta);
  }

  public PaginaResultado<PropuestaDto> buscarPropuestas(String perfilId, PropuestasFiltro filtros) {
    PaginaResultado<Propuesta> resultado = new PaginaResultado<>(
        new ArrayList<>(),
        0,
        0,
        0
    );

    if(filtros.tipo().equals("RECIBIDAS")) {
      resultado = this.repositorioPropuestas.buscarPorDestinatarioId(perfilId, filtros);
    } else if (filtros.tipo().equals("ENVIADAS")) {
      resultado = this.repositorioPropuestas.buscarPorAutorId(perfilId, filtros);
    }

    return new PaginaResultado<>(
        resultado.contenido().stream().map(PropuestaDto::new).toList(),
        resultado.cantidadDeElementos(),
        resultado.cantidadDePaginas(),
        resultado.numero());
  }
}