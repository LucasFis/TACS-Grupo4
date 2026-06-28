package app.model.entities;

import app.exceptions.BadRequestException;
import jdk.jfr.Experimental;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FiguritaIntercambiable {

    @DBRef
    private Figurita figurita;

    private Integer cantidadExistente;

    @Builder.Default
    private Integer cantidadReservada = 0;
    private List<MetodoIntercambio> metodos;

    @Experimental
    @Builder.Default
    private String perfilId = null;

    public FiguritaIntercambiable(Figurita figurita, Integer cantidadExistente, List<MetodoIntercambio> metodos) {
        this(figurita, cantidadExistente,0, metodos, null);
    }

    public FiguritaIntercambiable(Figurita figurita, Integer cantidadExistente, List<MetodoIntercambio> metodos, String perfilId) {
        this(figurita, cantidadExistente,0, metodos, perfilId);
    }

    /**
     * Verifica si esta figurita soporta el método de intercambio indicado.
     *
     * @param tipo método de intercambio a verificar
     * @return {@code true} si la figurita acepta ese método
     */
    public boolean soporta(MetodoIntercambio tipo) {
        return metodos.contains(tipo);
    }

    /**
     * Calcula la cantidad de ejemplares disponibles para intercambio
     * (existentes menos reservados).
     *
     * @return cantidad de figuritas disponibles
     */
    public int getCantidadDisponible() {
        return this.cantidadExistente - this.cantidadReservada;
    }

    /**
     * Reserva una unidad de esta figurita para un intercambio o subasta.
     * Valida que el método de intercambio sea soportado y que haya
     * suficiente cantidad disponible.
     *
     * @param metodo método de intercambio para el cual se reserva
     * @throws app.exceptions.BadRequestException si el método no es soportado
     *         o no hay unidades disponibles
     */
    public void reservar(MetodoIntercambio metodo) {
        this.validarMetodo(metodo);
        this.validarCantidadDisponible();
        this.cantidadReservada += 1;
    }

    /**
     * Elimina una reserva existente, disminuyendo el contador de reservas en 1.
     *
     * @throws app.exceptions.BadRequestException si no hay reservas para eliminar
     */
    public void eliminarReserva() {
        if (cantidadReservada <= 0) {
            throw new BadRequestException("No hay reservas para eliminar");
        }
        this.cantidadReservada -= 1;
    }

    /**
     * Concreta un intercambio: disminuye la cantidad existente en 1 y,
     * si había una reserva, la elimina.
     *
     * @throws app.exceptions.BadRequestException si no hay cantidad existente
     */
    public void cambioConcretado() {
        if (cantidadExistente <= 0) {
            throw new BadRequestException("No hay cantidad existente de esta figurita");
        }

        this.cantidadExistente -= 1;

        if (cantidadReservada > 0) {
            eliminarReserva();
        }
    }

    private void validarMetodo(MetodoIntercambio metodo) {
        if(!this.metodos.contains(metodo)) {
            throw new BadRequestException("Esta figurita no soporta el metodo seleccionado");
        }
    }

    private void validarCantidadDisponible() {
        if (this.getCantidadDisponible() <= 0) {
            throw new BadRequestException("No hay figuritas disponibles para reservar");
        }
    }
}
