package app.model.entities;

import app.exceptions.BadRequestException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "subastas")
@Builder
@CompoundIndex(name = "subasta_fechas_activa", def = "{'fechaInicio': 1, 'fechaCierre': 1}")
public class Subasta {

    @Id
    private String id;

    @DBRef
    private Perfil autor;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCierre;

    @DBRef
    private Figurita figuritaSubastada;

    @Builder.Default
    private List<Propuesta> ofertas = new ArrayList<>();;

    @DBRef
    @Builder.Default
    private List<Figurita> figuritasSolicitadas = new ArrayList<>();

    @Builder.Default
    private Integer calificacionMinimaSolicitada = 1;

    @Builder.Default
    private boolean avisoFinalEnviado = false;

    /**
     * Agrega una oferta a la subasta. Valida que se cumplan las condiciones mínimas
     * (figuritas solicitadas y calificación mínima) y reserva las figuritas ofrecidas.
     */
    public void agregarOferta(Propuesta oferta) {
        boolean tieneCondicionesSolicitadas = !this.figuritasSolicitadas.isEmpty();
        boolean noOfertaLasSolicitadas = this.figuritasSolicitadas.stream().noneMatch(fs -> oferta.getFiguritasOfrecidas().contains(fs));

        if(tieneCondicionesSolicitadas && (noOfertaLasSolicitadas || oferta.getAutor().getCalificacionMedia() < this.calificacionMinimaSolicitada)) {
            throw new BadRequestException("No se cumplieron las condiciones minimas");
        }
        oferta.getAutor().getColeccion().reservarRepetidas(oferta.getFiguritasOfrecidas(), MetodoIntercambio.SUBASTA);
        this.ofertas.add(oferta);
    }

    /**
     * Retorna {@code true} si la subasta está dentro de su ventana de tiempo
     */
    public Boolean estaActivo() {
        final LocalDateTime fechaActual = LocalDateTime.now();

        return fechaActual.isAfter(fechaInicio) && fechaActual.isBefore(fechaCierre);
    }

    /**
     * Cancela la subasta. Libera la reserva de la figurita subastada, rechaza la oferta
     * seleccionada (si existe) y rechaza todas las ofertas pendientes.
     */
    public void cancelar(String perfilId){
        this.autor.getColeccion().sacarReservasRepetidas(List.of(this.figuritaSubastada));
        Propuesta seleccionada = obtenerSeleccionada();
        if(seleccionada != null) {
            seleccionada.rechazar(perfilId);
        }

        rechazarOfertasPendientes(perfilId);
        finalizarSubasta();
    }

    /**
     * Cierra la subasta. Acepta la oferta seleccionada (si existe) o libera la reserva
     * de la figurita subastada, y rechaza todas las ofertas pendientes.
     */
    public void cerrar(String perfilId) {
        Propuesta seleccionada = obtenerSeleccionada();
        if(seleccionada != null) {
            seleccionada.aceptar(perfilId);
        }else {
            this.autor.getColeccion().sacarReservasRepetidas(List.of(this.figuritaSubastada));
        }

        rechazarOfertasPendientes(perfilId);
        finalizarSubasta();
    }
    /**
     * Reserva la figurita subastada en la colección del autor.
     */
    public void reservarFiguritaSubastada(){
        this.autor.getColeccion().reservarRepetidas(List.of(this.figuritaSubastada), MetodoIntercambio.SUBASTA);
    }
    /**
     * Selecciona una oferta como ganadora. Si había una oferta previamente seleccionada,
     * la deselecciona antes de seleccionar la nueva.
     */
    public void seleccionarOferta(String ofertaId, String perfilId){
        Propuesta oferta = this.getOfertas().stream()
            .filter(p -> p.getId().equals(ofertaId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Oferta no encontrada"));

        this.getOfertas().stream()
            .filter(p -> p.getEstadoActual().getValor() == EstadoProceso.SELECCIONADO)
            .findFirst()
            .ifPresent(p -> p.deseleccionar(perfilId));

        oferta.seleccionar(perfilId);
    }
    /**
     * Cancela una oferta de la subasta. Valida que {@code ofertaId} corresponda
     * a una oferta existente y que {@code perfilId} sea su autor.
     */
    public Propuesta cancelarOferta(String ofertaId, String perfilId){
        Propuesta oferta = getOfertas().stream()
            .filter(o -> o.getId().equals(ofertaId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Oferta no encontrada"));

        oferta.cancelar(perfilId);

        return oferta;
    }
    /**
     * Rechaza una oferta de la subasta. Valida que {@code ofertaId} corresponda
     * a una oferta existente y que {@code perfilId} sea el destinatario.
     */
    public Propuesta rechazarOferta(String ofertaId, String perfilId){
        Propuesta oferta = this.getOfertas().stream()
            .filter(p -> p.getId().equals(ofertaId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Oferta no encontrada"));

        oferta.rechazar(perfilId);

        return oferta;
    }

    private void rechazarOfertasPendientes(String perfilId) {
        getOfertas().stream()
            .filter(o -> o.getEstadoActual().getValor() == EstadoProceso.PENDIENTE)
            .forEach(o -> o.rechazar(perfilId));
    }
    /**
     * Modifica las figuritas ofrecidas en una oferta existente. Valida que {@code ofertaId}
     * corresponda a una oferta existente y delega la modificación en la propuesta.
     */
    public Propuesta modificarFiguritasDeOferta(String ofertaId, String perfilId, List<Figurita> nuevasFiguritas) {
        Propuesta oferta = getOfertas().stream()
            .filter(o -> o.getId().equals(ofertaId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Oferta no encontrada"));

        oferta.modificarFiguritas(perfilId, nuevasFiguritas, MetodoIntercambio.SUBASTA);

        return oferta;
    }

    private void finalizarSubasta() {
        this.setFechaCierre(LocalDateTime.now());
    }
    /**
     * Retorna la oferta actualmente seleccionada, o {@code null} si ninguna lo está.
     */
    public Propuesta obtenerSeleccionada() {
        return getOfertas().stream()
            .filter(p -> p.getEstadoActual().getValor() == EstadoProceso.SELECCIONADO)
            .findFirst()
            .orElse(null);
    }

    public boolean necesitaAvisoFinal() {
        if (avisoFinalEnviado) {
            return false;
        }

        LocalDateTime ahora = LocalDateTime.now();

        return estaActivo()
                && fechaCierre.isAfter(ahora)
                && fechaCierre.isBefore(ahora.plusHours(1));
    }
}

