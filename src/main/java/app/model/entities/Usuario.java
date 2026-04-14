package app.model.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Usuario {
    private String id;
    private String nombre;
    private Coleccion coleccion;
    private String telefono;
    private List<Integer> calificaciones;

    public Float getCalificacionMedia() {
        return (float) calificaciones.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }
}
