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

    public Subasta(Usuario usuario, LocalDateTime fechaInicio, LocalDateTime fechaCierre, Figurita figuritaSubastada, Propuesta propuestaGanadora) {
        this.usuario = usuario;
        this.fechaInicio = fechaInicio;
        this.fechaCierre = fechaCierre;
        this.figuritaSubastada = figuritaSubastada;
        this.propuestaGanadora = propuestaGanadora;
    }

    public Boolean estaActivo() {
        final LocalDateTime fechaActual = LocalDateTime.now();

        return fechaActual.isAfter(fechaInicio) && fechaActual.isBefore(fechaCierre);
    }

    public void algoritmoSeleccionador(Propuesta propuesta) {
        Propuesta propuestaActual = this.propuestaGanadora;

        if(propuestaActual == null) {
            this.propuestaGanadora = propuesta;
            return;
        }

        if(propuesta.getFiguritasOfrecidas().size() > propuestaActual.getFiguritasOfrecidas().size()) {
            this.propuestaGanadora = propuesta;
        }
    }
}

