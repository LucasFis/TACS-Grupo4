package app.model.entities;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Getter
@Setter
public class Subasta {
    private String id;
    private Usuario usuario;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCierre;
    private Figurita figuritaSubastada;
    private Propuesta propuestaGanadora;

    public Boolean estaActivo() {
        final LocalDateTime fechaActual = LocalDateTime.now();

        return fechaActual.isAfter(fechaInicio) && fechaActual.isBefore(fechaCierre);
    }

    public void algoritmoSeleccionador(Propuesta propuesta) {
        //TODO
    }
}
