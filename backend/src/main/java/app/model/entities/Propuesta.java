package app.model.entities;

import app.exceptions.BadRequestException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

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

    @DBRef(lazy = true)
    private List<Figurita> figuritasOfrecidas;

    @DBRef
    private Figurita figuritaBuscada;

    @Builder.Default
    private List<EstadoPropuesta> estado= new ArrayList<>(
        List.of(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE))
    );
    //Valído que no este pendiente y que solo lo pueda aceptar el usuario Correspondiente.
    //Chequear si eso está bien o no es necesario.

    /**
     * Retorna el estado más reciente de la propuesta. Si la lista está vacía
     * (puede ocurrir al deserializar), inicializa con PENDIENTE.
     */
    public EstadoPropuesta obtenerEstadoActual() {
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
     * y que la propuesta esté en estado ACEPTADO.
     */
    public void aceptar(String perfilId) {
        validarUsuarioDestino(perfilId);
        validarPendiente();
        estado.add(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.ACEPTADO));
    }

    /**
     * Se selecciona la propuesta. Valida que {@code usuario} sea el destinatario
     * y que la propuesta esté en estado SELECCIONADO. (En una subasta)
     */
    public void seleccionar(String perfilId) {
        validarUsuarioDestino(perfilId);
        validarPendiente();
        estado.add(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.SELECCIONADO));
    }

    /**
     * Rechaza la propuesta. Valida que {@code usuario} sea el destinatario
     * y que la propuesta esté en estado RECHAZADO.
     */
    public void rechazar(String perfilId) {
        validarUsuarioDestino(perfilId);
        validarPendiente();
        estado.add(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.RECHAZADO));
    }

    /**
     * Cancela la propuesta. Valida que {@code usuario} sea el autor
     * y que la propuesta esté en estado PENDIENTE.
     */
    public void cancelar(String perfilId) {
        this.validarUsuarioAutor(perfilId);
        validarPendiente();
        estado.add(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.CANCELADO));
    }

    /**
     * Resetea la propuesta a estado PENDIENTE. Se utiliza cuando el ofertante
     * modifica las figuritas ofrecidas y la oferta debe ser revisada nuevamente.
     */
    public void resetearAPendiente(String perfilId) {
        this.validarUsuarioAutor(perfilId);
        estado.add(new EstadoPropuesta(LocalDateTime.now(), EstadoProceso.PENDIENTE));
    }

    private void validarPendiente() {
        if (obtenerEstadoActual().getValor() != EstadoProceso.PENDIENTE) {
            throw new BadRequestException("La propuesta ya fue respondida");
        }
    }

    private void validarUsuarioDestino(String perfilId) {
        if (!this.destinatario.getId().equals(perfilId)) {
            throw new BadRequestException("Solo el destinatario puede responder la propuesta");
        }
    }

    private void validarUsuarioAutor(String perfilId) {
        if (!this.autor.getId().equals(perfilId)) {
            throw new BadRequestException("Solo el destinatario puede responder la propuesta");
        }
    }

}
