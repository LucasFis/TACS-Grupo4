package app.model.entities;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
