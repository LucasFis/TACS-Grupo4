package app.model.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Propuesta {
    private String id;
    private Usuario usuarioOrigen;
    private Usuario usuarioDestino;
    private List<Figurita> figuritasOfrecidas;
    private Figurita figuritaBuscada;
    private EstadoProceso estado;

    public Propuesta(Usuario usuarioOrigen, Usuario usuarioDestino, List<Figurita> figuritasOfrecidas, Figurita figuritaBuscada, EstadoProceso estado) {
        this.usuarioOrigen = usuarioOrigen;
        this.usuarioDestino = usuarioDestino;
        this.figuritasOfrecidas = figuritasOfrecidas;
        this.figuritaBuscada = figuritaBuscada;
        this.estado = EstadoProceso.PENDIENTE; //Hago que arranque en pendiente siempre.
    }


    //Valído que no este pendiente y que solo lo pueda aceptar el usuario Correspondiente.
    //Chequear si eso está bien o no es necesario.

    public void aceptar(Usuario usuario) {
        validarUsuarioDestino(usuario);
        validarPendiente();
        // TODO: no hago intercambio real todavía.
        this.estado = EstadoProceso.ACEPTADO;
    }

    public void rechazar(Usuario usuario) {
        validarUsuarioDestino(usuario);
        validarPendiente();
        this.estado = EstadoProceso.RECHAZADO;
    }

    private void validarPendiente() {
        if (this.estado != EstadoProceso.PENDIENTE) {
            throw new RuntimeException("La propuesta ya fue respondida");
        }
    }

    private void validarUsuarioDestino(Usuario usuario) {
        if (!this.usuarioDestino.getId().equals(usuario.getId())) {
            throw new RuntimeException("Solo el destinatario puede responder la propuesta");
        }
    }
}
