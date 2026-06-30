package app.dto;

import app.model.entities.Figurita;
import app.model.entities.Seleccion;
import lombok.Getter;

@Getter
public class FiguritaDto {
  private String id;
  private int numero;
  private String jugador;
  private Seleccion seleccion;
  private String imagenUrl;

  public FiguritaDto(Figurita figurita) {
    this.id = figurita.getId();
    this.numero = figurita.getNumero();
    this.jugador = figurita.getJugador();
    this.seleccion = figurita.getSeleccion();
    this.imagenUrl = figurita.getImagenUrl();
  }
}