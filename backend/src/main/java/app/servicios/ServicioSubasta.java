package app.servicios;

import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.EditarOfertaRequest;
import app.dto.subasta.MiSubastaActivaDto;
import app.dto.subasta.MiSubastaFinalizadaDto;
import app.dto.subasta.SubastaDto;
import app.dto.subasta.SubastaParticipoDto;
import app.exceptions.BadRequestException;
import app.exceptions.NotFoundException;
import app.model.entities.*;
import app.repositories.RepositorioCalificacion;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioSubastas;
import app.repositories.impl.campos.CamposColeccion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import app.repositories.impl.campos.CamposPerfil;
import app.repositories.impl.campos.CamposSubasta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioSubasta {
  private final RepositorioSubastas repoSubasta;
  private final RepositorioPerfiles repositorioPerfiles;
  private final RepositorioFiguritas repoFigurita;
  private final RepositorioCalificacion repoCalificacion;
  private final RepositorioColecciones repositorioColecciones;
  private final ServicioNotificacion notificacionService;

  /**
   * Crea una nueva subasta para una figurita repetida del perfil, con una duración
   * determinada, una lista de figuritas deseadas y una calificación mínima requerida
   * para participar. Notifica a los perfiles que tienen la figurita como faltante.
   *
   * @param perfilId            identificador del perfil que crea la subasta
   * @param figuritaId          identificador de la figurita a subastar
   * @param duracionEnHoras     duración de la subasta en horas
   * @param figuritasDeseadasIds lista de identificadores de figuritas deseadas a cambio
   * @param calificacionMinima  calificación mínima requerida para ofertar
   * @throws app.exceptions.NotFoundException si no se encuentra el perfil o alguna figurita
   */
  @Transactional
  public void crearSubasta(String perfilId, String figuritaId, Integer duracionEnHoras,
                           List<String> figuritasDeseadasIds, Integer calificacionMinima) {
    CamposPerfil conColeccion = new CamposPerfil(true);
    Perfil perfil = this.repositorioPerfiles.buscarPorId(perfilId, conColeccion);
    Figurita figuritaSubastada = this.repoFigurita.buscarPorId(figuritaId);

    List<Figurita> figuritasDeseadas = figuritasDeseadasIds.stream()
        .map(this.repoFigurita::buscarPorId)
        .toList();

    LocalDateTime fechaInicio = LocalDateTime.now();
    LocalDateTime fechaFin = fechaInicio.plusHours(duracionEnHoras.longValue());

    Subasta nuevaSubasta = Subasta.builder()
        .autor(perfil).figuritaSubastada(figuritaSubastada)
        .fechaInicio(fechaInicio).fechaCierre(fechaFin)
        .figuritasSolicitadas(figuritasDeseadas)
        .calificacionMinimaSolicitada(calificacionMinima)
        .build();

    nuevaSubasta.reservarFiguritaSubastada();

    this.repoSubasta.guardar(nuevaSubasta);
    this.repositorioColecciones.guardar(perfil.getColeccion(), new CamposColeccion(true, false));

    CamposPerfil conMedio = new CamposPerfil(true);
    List<Perfil> interesados = this.repositorioPerfiles
        .buscarPorFiguritaFaltante(figuritaSubastada, conMedio);

    this.notificacionService.notificarInteresados(
        interesados, "Encontramos una subasta de una figurita que te falta!");
  }

  /**
   * Realiza una oferta en una subasta activa ofreciendo un conjunto de figuritas
   * repetidas a cambio de la figurita subastada. Valida que la subasta esté activa,
   * que la figurita subastada sea faltante del ofertante y que no haya figuritas repetidas.
   *
   * @param autorId        identificador del perfil que realiza la oferta
   * @param subastaId      identificador de la subasta en la que se oferta
   * @param rawFiguritasId lista de identificadores de figuritas ofrecidas
   * @throws app.exceptions.BadRequestException si la subasta no está activa, la figurita
   *         subastada no es faltante del ofertante, o hay figuritas ofrecidas repetidas
   * @throws app.exceptions.NotFoundException  si no se encuentra la subasta o alguna figurita
   */
  @Transactional
  public void ofertarEnSubasta(String autorId, String subastaId, List<String> rawFiguritasId) {
    CamposPerfil conColeccion = new CamposPerfil(true);
    Perfil autor = this.repositorioPerfiles.buscarPorId(autorId, conColeccion);
    CamposSubasta camposSubasta = new CamposSubasta(true, false);
    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, camposSubasta);
    Perfil destinatario = subasta.getAutor();

    if (!subasta.estaActivo()) {
      throw new BadRequestException("La subasta ya cerro");
    }

    if (!autor.getColeccion().tieneFaltante(subasta.getFiguritaSubastada())) {
      throw new BadRequestException("La figurita subastada #" + subasta.getFiguritaSubastada().getNumero() + " no está en tus faltantes");
    }

    if (rawFiguritasId.size() != rawFiguritasId.stream().distinct().count()) {
      throw new BadRequestException("Figuritas ofrecidas repetidas");
    }

    List<Figurita> figuritasOfrecidas = rawFiguritasId.stream()
        .map(this.repoFigurita::buscarPorId)
        .toList();

    Propuesta nuevaPropuesta = Propuesta.builder()
        .id(UUID.randomUUID().toString())
        .autor(autor)
        .destinatario(destinatario)
        .figuritaBuscada(subasta.getFiguritaSubastada())
        .figuritasOfrecidas(figuritasOfrecidas)
        .build();

    subasta.agregarOferta(nuevaPropuesta);

    this.repositorioColecciones.guardar(autor.getColeccion(), new CamposColeccion(true, false));
    this.repoSubasta.guardar(subasta, camposSubasta);
  }

  /**
   * Modifica las figuritas ofrecidas en una oferta existente dentro de una subasta activa.
   *
   * @param perfilId  identificador del perfil propietario de la oferta
   * @param subastaId identificador de la subasta
   * @param ofertaId  identificador de la oferta a modificar
   * @param body      nuevas figuritas ofrecidas
   * @throws app.exceptions.BadRequestException si la oferta no pertenece al perfil
   * @throws app.exceptions.NotFoundException  si no se encuentra la subasta o la oferta
   */
  @Transactional
  public void editarOfertaEnSubasta(String perfilId, String subastaId, String ofertaId, EditarOfertaRequest body) {
    CamposSubasta camposSubasta = new CamposSubasta(true, false);
    CamposColeccion camposColeccion = new CamposColeccion(true, false);

    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, camposSubasta);

    // cargar colección antes de modificar
    Propuesta oferta = subasta.getOfertas().stream()
        .filter(o -> o.getId().equals(ofertaId))
        .findFirst()
        .orElseThrow(() -> new BadRequestException("Oferta no encontrada"));

    Coleccion coleccion = this.repositorioColecciones.buscarPorId(
        oferta.getAutor().getColeccion().getId(), camposColeccion);
    oferta.getAutor().setColeccion(coleccion);

    List<Figurita> nuevasFiguritas = this.repoFigurita.buscarPorIds(body.getFiguritasOfrecidasId());
    subasta.modificarFiguritasDeOferta(ofertaId, perfilId, nuevasFiguritas);

    this.repositorioColecciones.guardar(coleccion, camposColeccion);
    this.repoSubasta.guardar(subasta, camposSubasta);
  }

  /**
   * Cancela una oferta existente en una subasta activa, liberando las figuritas
   * que habían sido reservadas.
   *
   * @param perfilId  identificador del perfil que cancela la oferta
   * @param subastaId identificador de la subasta
   * @param ofertaId  identificador de la oferta a cancelar
   * @throws app.exceptions.BadRequestException si la subasta ya cerró
   * @throws app.exceptions.NotFoundException  si no se encuentra la subasta o la oferta
   */
  @Transactional
  public void cancelarOferta(String perfilId, String subastaId, String ofertaId) {
    CamposSubasta camposSubasta = new CamposSubasta(true, false);
    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, camposSubasta);

    if (!subasta.estaActivo()) {
      throw new BadRequestException("La subasta ya cerro");
    }

    Propuesta oferta = subasta.cancelarOferta(ofertaId, perfilId);

    CamposColeccion camposColeccion = new CamposColeccion(true, false);
    this.repositorioColecciones.guardar(oferta.getAutor().getColeccion(), camposColeccion);

    this.repoSubasta.guardar(subasta, camposSubasta);
  }

  /**
   * Selecciona una oferta como ganadora de la subasta. El autor de la subasta
   * elige cuál oferta aceptar.
   *
   * @param perfilId  identificador del perfil que selecciona la oferta (autor de la subasta)
   * @param subastaId identificador de la subasta
   * @param ofertaId  identificador de la oferta seleccionada
   * @throws app.exceptions.BadRequestException si la subasta ya cerró
   * @throws app.exceptions.NotFoundException  si no se encuentra la subasta o la oferta
   */
  @Transactional
  public void seleccionarOferta( String perfilId, String subastaId, String ofertaId) {
    CamposSubasta camposSubasta = new CamposSubasta(true, false);
    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, camposSubasta);

    if (!subasta.estaActivo()) {
      throw new BadRequestException("La subasta ya cerro");
    }

    subasta.seleccionarOferta(ofertaId, perfilId);

    this.repoSubasta.guardar(subasta, camposSubasta);
  }

  /**
   * Rechaza una oferta en una subasta activa, liberando las figuritas que habían
   * sido reservadas para dicha oferta.
   *
   * @param perfilId  identificador del perfil que rechaza la oferta (autor de la subasta)
   * @param subastaId identificador de la subasta
   * @param ofertaId  identificador de la oferta a rechazar
   * @throws app.exceptions.BadRequestException si la subasta ya cerró
   * @throws app.exceptions.NotFoundException  si no se encuentra la subasta o la oferta
   */
  @Transactional
  public void rechazarOferta(String perfilId, String subastaId, String ofertaId) {
    CamposSubasta camposSubasta = new CamposSubasta(true, false);
    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, camposSubasta);

    if (!subasta.estaActivo()) {
      throw new BadRequestException("La subasta ya cerro");
    }

    Propuesta oferta = subasta.rechazarOferta(ofertaId, perfilId);

    CamposColeccion camposColeccion = new CamposColeccion(true, false);
    this.repositorioColecciones.guardar(oferta.getAutor().getColeccion(), camposColeccion);

    this.repoSubasta.guardar(subasta, camposSubasta);
  }

  /**
   * Cancela una subasta activa por completo. Libera las figuritas reservadas de
   * todas las ofertas (excepto las ya canceladas) y las del autor de la subasta.
   *
   * @param perfilId  identificador del perfil que cancela la subasta (autor)
   * @param subastaId identificador de la subasta a cancelar
   * @throws app.exceptions.BadRequestException si la subasta ya cerró
   * @throws app.exceptions.NotFoundException  si no se encuentra la subasta
   */
  @Transactional
  public void cancelarSubasta(String perfilId, String subastaId) {
    CamposSubasta camposSubasta = new CamposSubasta(true, false, true);
    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, camposSubasta);
    if (!subasta.estaActivo()) {
      throw new BadRequestException("La subasta ya cerro");
    }
    subasta.cancelar(perfilId);

    CamposColeccion camposColeccion = new CamposColeccion(true, false);
    subasta.getOfertas().stream()
        .filter(o -> o.getEstadoActual().getValor() != EstadoProceso.CANCELADO)
        .map(o -> o.getAutor().getColeccion())
        .distinct()
        .forEach(col -> this.repositorioColecciones.guardar(col, camposColeccion));

    this.repositorioColecciones.guardar(subasta.getAutor().getColeccion(), camposColeccion);
    this.repoSubasta.guardar(subasta, camposSubasta);
  }

  /**
   * Cierra una subasta activa. Si hay una oferta seleccionada, ejecuta el intercambio
   * entre el ganador y el autor. Las ofertas no seleccionadas se rechazan y se liberan
   * sus figuritas reservadas.
   *
   * @param perfilId  identificador del perfil que cierra la subasta (autor)
   * @param subastaId identificador de la subasta a cerrar
   * @throws app.exceptions.BadRequestException si la subasta ya cerró
   * @throws app.exceptions.NotFoundException  si no se encuentra la subasta
   */
  @Transactional
  public void cerrarSubasta(String perfilId, String subastaId) {
    CamposSubasta camposSubasta = new CamposSubasta(true, false, true);
    CamposColeccion todo = new CamposColeccion(true, true);
    CamposColeccion soloRepetidas = new CamposColeccion(true, false);

    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, camposSubasta);
    if (!subasta.estaActivo()) {
      throw new BadRequestException("La subasta ya cerro");
    }

    Propuesta seleccionada = subasta.obtenerSeleccionada();

    // cargar colección del ganador y del autor de la subasta
    if (seleccionada != null) {
      Coleccion colGanador = repositorioColecciones.buscarPorId(
          seleccionada.getAutor().getColeccion().getId(), todo);
      seleccionada.getAutor().setColeccion(colGanador);

      Coleccion colAutor = repositorioColecciones.buscarPorId(
          subasta.getAutor().getColeccion().getId(), todo);
      subasta.getAutor().setColeccion(colAutor);
      seleccionada.getDestinatario().setColeccion(colAutor); // misma instancia
    }

    // cargar colecciones de los ofertantes rechazados
    subasta.getOfertas().stream()
        .filter(o -> o.getEstadoActual().getValor() != EstadoProceso.CANCELADO)
        .filter(o -> seleccionada == null || !o.getId().equals(seleccionada.getId()))
        .forEach(o -> {
          Coleccion col = repositorioColecciones.buscarPorId(
              o.getAutor().getColeccion().getId(), soloRepetidas);
          o.getAutor().setColeccion(col);
        });

    subasta.cerrar(perfilId);

    subasta.getOfertas().stream()
        .filter(o -> seleccionada == null || !o.getId().equals(seleccionada.getId()))
        .filter(o -> o.getEstadoActual().getValor() != EstadoProceso.CANCELADO)
        .map(o -> o.getAutor().getColeccion())
        .distinct()
        .forEach(col -> this.repositorioColecciones.guardar(col, soloRepetidas));

    if (seleccionada != null) {
      this.repositorioColecciones.guardar(seleccionada.getAutor().getColeccion(), todo);
      this.repositorioColecciones.guardar(subasta.getAutor().getColeccion(), todo);
    }

    this.repoSubasta.guardar(subasta, camposSubasta);
  }

  /**
   * Obtiene las subastas del sistema según los filtros aplicados. El tipo de DTO
   * varía según el contexto:
   * <ul>
   *   <li>Si {@code filtros.participanteId()} no es {@code null}, devuelve subastas
   *       en las que el perfil participó como {@link app.dto.subasta.SubastaParticipoDto}.</li>
   *   <li>Si el estado es {@code ACTIVA}, devuelve las subastas activas del perfil
   *       como {@link app.dto.subasta.MiSubastaActivaDto}.</li>
   *   <li>En caso contrario, devuelve las subastas finalizadas del perfil como
   *       {@link app.dto.subasta.MiSubastaFinalizadaDto}.</li>
   * </ul>
   *
   * @param perfilId identificador del perfil que solicita las subastas
   * @param filtros  criterios de filtrado (estado, participante, paginación)
   * @return página de subastas según el tipo de vista solicitada
   */
    public PaginaResultado<?> obtenerSubastas(String perfilId, SubastasFiltro filtros) {
      PaginaResultado<Subasta> resultado = this.repoSubasta.buscarTodos(filtros, new CamposSubasta(true, true));

      if (filtros.participanteId() != null) {
        return resultado.mapearA(s -> {
          boolean yaCalifico = this.repoCalificacion.yaCalifico(
              s.getAutor().getId(),
              perfilId,
              s.getId(),
              MetodoIntercambio.SUBASTA
          );
          return new SubastaParticipoDto(s, obtenerOferta(perfilId, s), yaCalifico);
        });

      } else if ("ACTIVA".equals(filtros.estado())) {
        return resultado.mapearA(MiSubastaActivaDto::new);

      } else {
        return resultado.mapearA(s -> {
          String ganadorId = s.getOfertas().stream()
              .filter(o -> o.getEstadoActual().getValor() == EstadoProceso.ACEPTADO)
              .findFirst()
              .map(o -> o.getAutor().getId())
              .orElse(null);

          boolean yaCalifico = ganadorId != null && this.repoCalificacion.yaCalifico(
              ganadorId,
              perfilId,
              s.getId(),
              MetodoIntercambio.SUBASTA
          );

          return new MiSubastaFinalizadaDto(s, yaCalifico);
        });
      }
    }

  /**
   * Busca la oferta realizada por un perfil dentro de una subasta específica.
   *
   * @param perfilId identificador del perfil del cual se busca la oferta
   * @param subasta  subasta en la cual buscar la oferta
   * @return la oferta del perfil en la subasta
   * @throws app.exceptions.NotFoundException si el perfil no tiene una oferta en la subasta
   */
  private Propuesta obtenerOferta(String perfilId, Subasta subasta) {
    return subasta.getOfertas().stream()
        .filter(p -> p.getAutor().getId().equals(perfilId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("No se encontró oferta del perfil " + perfilId + " en la subasta " + subasta.getId()));
  }

  /**
   * Obtiene los datos completos de una subasta por su identificador.
   *
   * @param subastaId identificador de la subasta a obtener
   * @return datos de la subasta como {@link SubastaDto}
   * @throws app.exceptions.NotFoundException si no se encuentra la subasta
   */
  public SubastaDto obtenerSubasta(String subastaId) {
    Subasta subasta = this.repoSubasta.buscarPorId(subastaId, new CamposSubasta(true, true));

    return new SubastaDto(subasta);
  }
}

