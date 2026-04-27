package app.model.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Perfil {
    private String id;
    private Usuario usuario;
    private String nombre;
    private Coleccion coleccion;
    private List<MedioDeContacto> mediosDeContacto;
    private List<Calificacion> calificaciones;

    public Float obtenerCalificacionMedia() {
        return (float) calificaciones.stream()
            .mapToInt(Calificacion::getValor)
            .average()
            .orElse(0.0);
    }
}
