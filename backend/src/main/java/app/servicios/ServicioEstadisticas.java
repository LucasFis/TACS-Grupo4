package app.servicios;

import app.dto.*;
import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.exceptions.UnauthorizedException;
import app.model.entities.EstadoProceso;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Propuesta;
import app.model.entities.Subasta;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioPerfiles;
import app.repositories.RepositorioPropuestas;
import app.repositories.RepositorioRankings;
import app.repositories.RepositorioSubastas;
import app.repositories.impl.campos.CamposSubasta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioEstadisticas {

    private final RepositorioPerfiles repositorioPerfiles;
    private final RepositorioPropuestas repositorioPropuestas;
    private final RepositorioSubastas repositorioSubastas;
    private final RepositorioColecciones repositorioColecciones;
    private final RepositorioRankings repositorioRankings;

    private static final int TOP = 3;
    private static final int MINIMO_PROPUESTAS_TASA = 3;
    private static final int MINIMO_CALIFICACIONES = 1;

  /**
   * Obtiene las estadísticas generales del sistema, incluyendo cantidad de usuarios,
   * figuritas publicadas, propuestas, subastas activas, y desgloses por estado de
   * propuesta y por modalidad de intercambio. Solo puede ser invocado por un usuario
   * con rol de administrador.
   *
   * @param dto datos de la sesión del usuario que solicita las estadísticas, utilizados
   *            para validar que tenga rol de administrador
   * @return un {@link EstadisticasDto} con el resumen de las estadísticas del sistema
   * @throws UnauthorizedException si el rol del usuario no es {@code ADMINISTRADOR}
   */
    public EstadisticasDto obtenerEstadisticas(SesionDto dto, LocalDate desde, LocalDate hasta) {

        if(!"ADMINISTRADOR".equals(dto.rol())) {
            throw new UnauthorizedException("Solo el admin puede ver las estadisticas");
        }

        LocalDateTime desdeDateTime = desde.atStartOfDay();
        LocalDateTime hastaDateTime = hasta.atTime(LocalTime.MAX);

        long totalUsuarios = repositorioPerfiles.contar();

        long totalFiguritasPublicadas = this.repositorioColecciones.contarRepetidas(new ArrayList<>());

        List<Propuesta> propuestasPeriodo =
            repositorioPropuestas.buscarEstadisticasPorRango(desdeDateTime, hastaDateTime);

        int totalPropuestas = propuestasPeriodo.size();

        SubastasFiltro filtros = new SubastasFiltro(0, 20, null, null, "ACTIVA");

        PaginaResultado<Subasta> totalSubastasActivas = repositorioSubastas.buscarTodos(filtros, new CamposSubasta(false, false));

        PropuestasPorEstadoDto propuestasPorEstado = calcularPropuestasPorEstado(propuestasPeriodo);

        FiguritasPorModalidadDto figuritasPorModalidad = calcularFiguritasPorModalidad();

        RankingsDto rankings = calcularRankings(desdeDateTime, hastaDateTime);

        return new EstadisticasDto(
            totalUsuarios,
            totalFiguritasPublicadas,
            totalPropuestas,
            (int) totalSubastasActivas.cantidadDeElementos(),
            propuestasPorEstado,
            figuritasPorModalidad,
            rankings
        );
    }

  /**
   * Calcula los rankings de usuarios (leaderboards). Las métricas de actividad
   * (propuestas, intercambios, tasa de aceptación y subastas) se filtran por el
   * período indicado; la reputación y el volumen de colección son globales.
   */
    private RankingsDto calcularRankings(LocalDateTime desde, LocalDateTime hasta) {
        return new RankingsDto(
            repositorioRankings.topCreadoresDePropuestas(desde, hasta, TOP),
            repositorioRankings.topIntercambiadores(desde, hasta, TOP),
            repositorioRankings.mejorTasaAceptacion(desde, hasta, MINIMO_PROPUESTAS_TASA, TOP),
            repositorioRankings.topSubastadores(desde, hasta, TOP),
            repositorioRankings.mejorReputacion(MINIMO_CALIFICACIONES, TOP),
            repositorioRankings.topColeccionistas(TOP)
        );
    }

  /**
   * Calcula la cantidad de propuestas agrupadas según su estado actual
   * (pendiente, aceptada o rechazada).
   *
   * @return un {@link PropuestasPorEstadoDto} con la cantidad de propuestas
   *         en cada uno de los estados posibles
   */
    private PropuestasPorEstadoDto calcularPropuestasPorEstado(List<Propuesta> propuestas) {
        Map<EstadoProceso, Long> porEstado = propuestas.stream()
            .collect(Collectors.groupingBy(p -> p.getEstadoActual().getValor(), Collectors.counting()));

        return new PropuestasPorEstadoDto(
            porEstado.getOrDefault(EstadoProceso.PENDIENTE, 0L).intValue(),
            porEstado.getOrDefault(EstadoProceso.SELECCIONADO, 0L).intValue(),
            porEstado.getOrDefault(EstadoProceso.ACEPTADO, 0L).intValue(),
            porEstado.getOrDefault(EstadoProceso.RECHAZADO, 0L).intValue(),
            porEstado.getOrDefault(EstadoProceso.CANCELADO, 0L).intValue()
        );
    }

    /**
     * Calcula la cantidad de figuritas repetidas disponibles según la modalidad
     * de intercambio que aceptan: únicamente intercambio, únicamente subasta,
     * o ambas modalidades a la vez.
     *
     * @return un {@link FiguritasPorModalidadDto} con la cantidad de figuritas
     *         repetidas para cada modalidad
     */
    private FiguritasPorModalidadDto calcularFiguritasPorModalidad() {
        long soloIntercambio = this.repositorioColecciones
            .contarRepetidas(List.of(MetodoIntercambio.INTERCAMBIO));

        long soloSubasta = this.repositorioColecciones
            .contarRepetidas(List.of(MetodoIntercambio.SUBASTA));

        long ambos = this.repositorioColecciones
            .contarRepetidas(List.of(MetodoIntercambio.SUBASTA, MetodoIntercambio.INTERCAMBIO));

        return new FiguritasPorModalidadDto(soloIntercambio, soloSubasta, ambos);

    }
}