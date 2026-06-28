package app.model.entities;

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
@Builder
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "perfiles")
public class Perfil {

    @Id
    private String id;

    @DBRef
    private Usuario usuario;

    private String nombre;

    @DBRef
    @Builder.Default
    private Coleccion coleccion = new Coleccion();

    @Builder.Default
    private List<MedioDeContacto> mediosDeContacto = new ArrayList<>();

    @Builder.Default
    private Double calificacionMedia = 0.0;
    @Builder.Default
    private int cantidadCalificaciones = 0;

    /**
     * Agrega una nueva calificación en la cantidad acumulada y actualiza el promedio.
     */
    public void agregarNuevaCalificacion(Calificacion calificacion) {
        this.cantidadCalificaciones++;
        this.calificacionMedia = this.calificacionMedia +
            (calificacion.getValor() - this.calificacionMedia) / this.cantidadCalificaciones;
    }

    public String getIniciales() {
        return this.nombre
            .substring(0, Math.min(2, this.nombre.length()))
            .toUpperCase();
    }
}
