package app.model.entities;

import app.exceptions.BadRequestException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import app.exceptions.ForbiddenException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AccessLevel;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "propuestas")
@Builder
public class Propuesta {

    @Id
    private String id;

    @DBRef
    private Perfil autor;

    @DBRef
    private Perfil destinatario;

    @DBRef
    private List<Figurita> figuritasOfrecidas;

    @DBRef
    private Figurita figuritaBuscada;

    @Builder.Default
    private List<EstadoPropuesta> estado = new ArrayList<>(
        List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))
    );

    @Getter(AccessLevel.NONE)
    @Builder.Default
    private EstadoPropuesta estadoActual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE);

    public EstadoPropuesta getEstadoActual() {
        if (estado == null || estado.isEmpty()) {
            EstadoPropuesta inicial = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE);
            estado = new ArrayList<>();
            estado.add(inicial);
            return inicial;
        }
        return estado.get(estado.size() - 1);
    }

    /**
     * Acepta la propuesta. Valida que {@code usuario} sea el destinatario
     * y que la propuesta esté en estado PENDIENTE o SELECCIONADO.
     */
    public void aceptar(String perfilId) {
        validarUsuarioDestino(perfilId);
        validarAceptable();
        ejecutarIntercambio();
        EstadoPropuesta actual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.ACEPTADO);
        estado.add(actual);
        setEstadoActual(actual);
    }

    /**
     * Se selecciona la propuesta. Valida que {@code usuario} sea el destinatario
     * y que la propuesta esté en estado PENDIENTE. (En una subasta)
     */
    public void seleccionar(String perfilId) {
        validarUsuarioDestino(perfilId);
        validarPendiente();
        EstadoPropuesta actual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.SELECCIONADO);
        estado.add(actual);
        setEstadoActual(actual);
    }

    /**
     * Rechaza la propuesta. Valida que {@code usuario} sea el destinatario
     * y que la propuesta esté en estado PENDIENTE o SELECCIONADO.
     */
    public void rechazar(String perfilId) {
        validarUsuarioDestino(perfilId);
        validarPendienteOSeleccionada();
        ejecutarRechazo();
        EstadoPropuesta actual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.RECHAZADO);
        estado.add(actual);
        setEstadoActual(actual);
    }

    /**
     * Cancela la propuesta. Valida que {@code usuario} sea el autor.
     */
    public void cancelar(String perfilId) {
        validarUsuarioAutor(perfilId);
        ejecutarRechazo();
        EstadoPropuesta actual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.CANCELADO);
        estado.add(actual);
        setEstadoActual(actual);
    }
    /**
     * Modifica las figuritas ofrecidas por el autor. Libera las reservas anteriores,
     * asigna las nuevas figuritas y las reserva según el método de intercambio indicado.
     * Resetea el estado de la propuesta a PENDIENTE para que el destinatario la revise nuevamente.
     *
     * @param perfilId       ID del perfil que intenta modificar la propuesta
     * @param nuevasFiguritas lista de figuritas que reemplazarán a las ofrecidas actualmente
     * @param metodo         método de intercambio usado para reservar las nuevas figuritas
     * @throws ForbiddenException si {@code perfilId} no es el autor
     */
    public void modificarFiguritas(String perfilId, List<Figurita> nuevasFiguritas,  MetodoIntercambio metodo) {
        validarUsuarioAutor(perfilId);
        ejecutarRechazo();
        this.figuritasOfrecidas = nuevasFiguritas;
        this.autor.getColeccion().reservarRepetidas(nuevasFiguritas, metodo);
        EstadoPropuesta actual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE);
        estado.add(actual);
        setEstadoActual(actual);
    }

    private void ejecutarIntercambio() {
        this.getFiguritasOfrecidas()
            .forEach(f -> this.destinatario.getColeccion().eliminarFaltante(f));
        this.destinatario.getColeccion()
            .descontarRepetida(this.getFiguritaBuscada());

        this.getFiguritasOfrecidas()
            .forEach(f -> this.autor.getColeccion().descontarRepetida(f));
        this.autor.getColeccion().eliminarFaltante(this.getFiguritaBuscada());
    }

    private void ejecutarRechazo() {
        this.autor.getColeccion().sacarReservasRepetidas(this.getFiguritasOfrecidas());
    }

    /**
     * Resetea la propuesta a estado PENDIENTE. Se utiliza cuando el ofertante
     * modifica las figuritas ofrecidas y la oferta debe ser revisada nuevamente.
     */
    public void resetearAPendiente(String perfilId) {
        validarUsuarioAutor(perfilId);
        ejecutarRechazo();
        EstadoPropuesta actual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE);
        estado.add(actual);
        setEstadoActual(actual);
    }

    /**
     * Deselecciona la propuesta, volviendo a PENDIENTE. Lo ejecuta el dueño de la subasta.
     */
    public void deseleccionar(String perfilId) {
        validarUsuarioDestino(perfilId);
        EstadoPropuesta actual = new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE);
        estado.add(actual);
        setEstadoActual(actual);
    }

    private void validarPendiente() {
        if (getEstadoActual().getValor() != EstadoProceso.PENDIENTE) {
            throw new BadRequestException("La propuesta ya fue respondida");
        }
    }

    private void validarPendienteOSeleccionada() {
        EstadoProceso estado = getEstadoActual().getValor();
        if (estado != EstadoProceso.PENDIENTE && estado != EstadoProceso.SELECCIONADO) {
            throw new BadRequestException("La propuesta ya fue respondida");
        }
    }

    private void validarAceptable() {
        EstadoProceso estado = getEstadoActual().getValor();

        if (
            estado != EstadoProceso.PENDIENTE &&
                estado != EstadoProceso.SELECCIONADO
        ) {
            throw new BadRequestException(
                "La propuesta ya fue respondida"
            );
        }
    }

    private void validarUsuarioDestino(String perfilId) {
        if (!this.destinatario.getId().equals(perfilId)) {
            throw new ForbiddenException("Solo el destinatario puede responder la propuesta");
        }
    }

    private void validarUsuarioAutor(String perfilId) {
        if (!this.autor.getId().equals(perfilId)) {
            throw new ForbiddenException("Solo el autor puede responder la propuesta");
        }
    }

}
