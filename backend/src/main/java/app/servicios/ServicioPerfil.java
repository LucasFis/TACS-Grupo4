package app.servicios;

import app.dto.*;
import app.dto.calificaciones.CalificacionesDto;
import app.dto.paginacion.PaginaResultado;
import app.dto.filtros.SugerenciasFiltro;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.Calificacion;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Sugerencia;
import app.model.entities.Perfil;
import app.repositories.RepositorioCalificacion;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioSubastas;
import app.repositories.RepositorioPerfiles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.repositories.impl.campos.CamposPerfil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioPerfil {

  private final RepositorioCalificacion repositorioCalificacion;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioNotificaciones repositorioNotificaciones;


  public List<FiguritaDto> obtenerFaltantes(String userId) {
    CamposPerfil campos = new CamposPerfil(false);

    Perfil perfil = repositorioPerfiles.buscarPorUsuarioId(userId, campos);
    return perfil.getColeccion().getFaltantes().stream()
        .map(FiguritaDto::new)
        .toList();
  }

  @Transactional
  public void agregarCalificacion(String autorId, String destinoId,
                                  Integer valor, String descripcion, String transaccionId,
                                  MetodoIntercambio tipoTransaccion) {
    if (valor == null) {
      throw new BadRequestException("El valor de la calificación no puede ser nulo");
    }
    if (valor < 1 || valor > 5) {
      throw new BadRequestException("El valor de la calificación debe estar entre 1 y 5");
    }

    CamposPerfil sinCampos = new CamposPerfil(false);

    Perfil perfilDestino = this.repositorioPerfiles.buscarPorId(destinoId, sinCampos);
    Perfil autor = this.repositorioPerfiles.buscarPorId(autorId, sinCampos);

    boolean yaCalifico = this.repositorioCalificacion.yaCalifico(
        destinoId,
        autorId,
        transaccionId,
        tipoTransaccion
    );

    if (yaCalifico) throw new BadRequestException("Ya calificaste esta transacción");

    Calificacion calificacion = Calificacion.builder()
        .autor(autor)
        .destinatario(perfilDestino)
        .valor(valor)
        .descripcion(descripcion)
        .tipoTransaccion(tipoTransaccion)
        .transaccionId(transaccionId)
        .build();

    perfilDestino.agregarNuevaCalificacion(calificacion);

    this.repositorioCalificacion.guardar(calificacion);
    this.repositorioPerfiles.guardar(perfilDestino, sinCampos);
  }

  public PaginaResultado<SugerenciaDto> obtenerSugerencias(String perfilId, SugerenciasFiltro filtros) {
    CamposPerfil campos = new CamposPerfil(false);
    Perfil perfilObjetivo = this.repositorioPerfiles.buscarPorId(perfilId, campos);

    PaginaResultado<Sugerencia> sugerencias = this.repositorioPerfiles.generarSugerencias(perfilObjetivo.getColeccion(), filtros);

    //TODO: sigue en implementacion
    return new PaginaResultado<>(sugerencias.contenido().stream().map(SugerenciaDto::new).toList(), 0, 0, 0);
  }

  public List<ContadorDto> obtenerContadores(String perfilId) {
    CamposPerfil campos = new CamposPerfil(false);
    Perfil perfil = this.repositorioPerfiles.buscarPorId(perfilId, campos);

    List<ContadorDto> contadores = new ArrayList<>();

    contadores.add(new ContadorDto("repetidas",perfil.getColeccion().getRepetidas().size()));
    contadores.add(new ContadorDto("faltantes",perfil.getColeccion().getFaltantes().size()));

    return contadores;
  }

  public List<NotificacionesDto> obtenerNotificaciones(String userId) {
    CamposPerfil sinCampos = new CamposPerfil(false);
    Perfil perfil = repositorioPerfiles.buscarPorId(userId, sinCampos);

    return this.repositorioNotificaciones.buscarPorPerfil(perfil).stream().map(NotificacionesDto::new).toList();
  }

  public PerfilDto obtenerPerfil(String perfilId) {
    Perfil perfil = this.repositorioPerfiles.buscarPorId(perfilId, new CamposPerfil(true));
    if (perfil == null) throw new NotFoundException("Perfil no encontrado para el usuario: " + perfilId);
    return new PerfilDto(perfil);
  }

  public PaginaResultado<CalificacionDto> obtenerCalificaciones(String perfilId, Integer pagina, Integer limite) {
    PaginaResultado<Calificacion> resultado = this.repositorioCalificacion.buscarPorDestinatario(perfilId, pagina, limite);

    return new PaginaResultado<>(
        resultado.contenido().stream().map(CalificacionDto::new).toList(),
        resultado.cantidadDeElementos(),
        resultado.cantidadDePaginas(),
        resultado.numero());
  }
}
