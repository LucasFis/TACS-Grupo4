package app.servicios;

import app.dto.CalificacionDto;
import app.dto.ContadorDto;
import app.dto.FiguritaDto;
import app.dto.NotificacionDto;
import app.dto.PerfilDto;
import app.dto.SugerenciaDto;
import app.dto.filtros.SugerenciasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.MedioDeContactoRequest;
import app.dto.request.PerfilRequest;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.Calificacion;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Sugerencia;
import app.model.entities.Usuario;
import app.repositories.RepositorioCalificacion;
import app.repositories.RepositorioNotificaciones;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioUsuarios;
import app.repositories.impl.campos.CamposPerfil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioPerfil {

  private final RepositorioCalificacion repositorioCalificacion;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioNotificaciones repositorioNotificaciones;
  private final ServicioNotificacion servicioNotificacion;
  private final RepositorioUsuarios repositorioUsuarios;


  /**
   * Obtiene la lista de figuritas faltantes del perfil asociado a un usuario.
   *
   * @param userId identificador del usuario del cual se obtendrán las faltantes
   * @return lista de figuritas faltantes del perfil del usuario
   * @throws app.exceptions.NotFoundException si no se encuentra un perfil para el {@code userId} indicado
   */
  public List<FiguritaDto> obtenerFaltantes(String userId) {
    CamposPerfil campos = new CamposPerfil(false);

    Perfil perfil = repositorioPerfiles.buscarPorUsuarioId(userId, campos);
    return perfil.getColeccion().getFaltantes().stream()
        .map(FiguritaDto::new)
        .toList();
  }


  /**
   * Agrega una calificación de un perfil a otro como parte de una transacción
   * (intercambio o subasta). Valida que el valor esté entre 1 y 5, y que el
   * autor no haya calificado previamente la misma transacción.
   *
   * @param autorId        identificador del perfil que emite la calificación
   * @param destinoId      identificador del perfil que recibe la calificación
   * @param valor          puntuación de la calificación (1-5)
   * @param descripcion    comentario opcional de la calificación
   * @param transaccionId  identificador de la transacción asociada
   * @param tipoTransaccion tipo de transacción (INTERCAMBIO o SUBASTA)
   * @throws app.exceptions.BadRequestException si el valor es nulo, está fuera del rango 1-5,
   *         o si el autor ya calificó esta transacción
   * @throws app.exceptions.NotFoundException si alguno de los perfiles indicados no existe
   */
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

    // Notificación al destinatario de que fue calificado
    String cuerpo = autor.getNombre() + " te calificó con " + valor + " estrella" + (valor == 1 ? "" : "s");
    servicioNotificacion.notificarInteresados(List.of(perfilDestino), cuerpo, "/perfil");
  }

  /**
   * Obtiene los contadores de figuritas repetidas y faltantes de un perfil.
   *
   * @param perfilId identificador del perfil del cual se obtendrán los contadores
   * @return lista con dos contadores: cantidad de repetidas y cantidad de faltantes
   * @throws app.exceptions.NotFoundException si no se encuentra el perfil indicado
   */
  public List<ContadorDto> obtenerContadores(String perfilId) {
    CamposPerfil campos = new CamposPerfil(false);
    Perfil perfil = this.repositorioPerfiles.buscarPorId(perfilId, campos);

    List<ContadorDto> contadores = new ArrayList<>();

    contadores.add(new ContadorDto("repetidas",perfil.getColeccion().getRepetidas().size()));
    contadores.add(new ContadorDto("faltantes",perfil.getColeccion().getFaltantes().size()));

    return contadores;
  }

  /**
   * Obtiene las notificaciones de un perfil convertidas a DTO.
   *
   * @param perfilId identificador del perfil del cual se obtendrán las notificaciones
   * @return lista de notificaciones del perfil como {@link NotificacionDto}
   */
  public List<NotificacionDto> obtenerNotificaciones(String perfilId) {
    return this.servicioNotificacion.obtenerPorPerfil(perfilId)
        .stream()
        .map(NotificacionDto::new)
        .toList();
  }

  /**
   * Obtiene los datos completos de un perfil, incluyendo los medios de contacto.
   *
   * @param perfilId identificador del perfil a obtener
   * @return datos del perfil como {@link PerfilDto}
   * @throws app.exceptions.NotFoundException si no se encuentra el perfil indicado
   */
  public PerfilDto obtenerPerfil(String perfilId) {
    Perfil perfil = this.repositorioPerfiles.buscarPorId(perfilId, new CamposPerfil(true));
    if (perfil == null) throw new NotFoundException("Perfil no encontrado para el usuario: " + perfilId);
    return new PerfilDto(perfil);
  }

  /**
   * Edita los datos de un perfil: nombre, nombre de usuario y/o medios de contacto.
   * Si se cambia el nombre de usuario, valida que no esté en uso.
   *
   * @param perfilId identificador del perfil a editar
   * @param body     datos actualizados del perfil
   * @throws app.exceptions.BadRequestException si el nuevo nombre de usuario ya está en uso
   * @throws app.exceptions.NotFoundException  si no se encuentra el perfil indicado
   */
  @Transactional
  public void editarPerfil(String perfilId, PerfilRequest body) {
    boolean actualizaMedios = body.getMediosDeContacto() != null;
    Perfil perfil = this.repositorioPerfiles.buscarPorId(perfilId, new CamposPerfil(actualizaMedios));
    Usuario usuario = perfil.getUsuario();

    boolean cambiaNombreUsuario = body.getNombreUsuario() != null
        && !body.getNombreUsuario().equals(usuario.getNombre());

    if (cambiaNombreUsuario) {
      try {
        this.repositorioUsuarios.guardar(
            new Usuario(usuario.getId(), usuario.getRol(), body.getNombreUsuario(), usuario.getContrasenia())
        );
      } catch (DuplicateKeyException e) {
        throw new BadRequestException("El nombre de usuario ya está en uso");
      }
    }

    if (body.getNombre() != null) {
      perfil.setNombre(body.getNombre());
    }
    if (actualizaMedios) {
      perfil.setMediosDeContacto(body.getMediosDeContacto().stream()
          .map(MedioDeContactoRequest::toEntity).toList());
    }
    this.repositorioPerfiles.guardar(perfil, new CamposPerfil(actualizaMedios));
  }


  /**
   * Obtiene las calificaciones recibidas por un perfil, de forma paginada.
   *
   * @param perfilId identificador del perfil del cual se obtendrán las calificaciones
   * @param pagina   número de página solicitado (base 0)
   * @param limite   cantidad máxima de resultados por página
   * @return página de calificaciones recibidas por el perfil
   */
  public PaginaResultado<CalificacionDto> obtenerCalificaciones(String perfilId, Integer pagina, Integer limite) {
    PaginaResultado<Calificacion> resultado = this.repositorioCalificacion.buscarPorDestinatario(perfilId, pagina, limite);

    return new PaginaResultado<>(
        resultado.contenido().stream().map(CalificacionDto::new).toList(),
        resultado.cantidadDeElementos(),
        resultado.cantidadDePaginas(),
        resultado.numero());
  }

  /**
   * Marca todas las notificaciones de un perfil como leídas.
   *
   * @param perfilId identificador del perfil cuyas notificaciones se marcarán como leídas
   */
  public void marcarTodasNotifsLeidas(String perfilId) {
      servicioNotificacion.marcarTodasLeidas(perfilId);
  }
}
