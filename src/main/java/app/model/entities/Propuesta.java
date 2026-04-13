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

}
