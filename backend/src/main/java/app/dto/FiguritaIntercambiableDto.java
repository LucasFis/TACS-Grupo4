package app.dto;

import app.model.entities.FiguritaIntercambiable;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Perfil;
import app.model.entities.Seleccion;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class FiguritaIntercambiableDto {
  private String figuritaId;
  private Integer numero;
  private String jugador;
  private Seleccion seleccion;
  private Integer cantidadExistente;
  private Integer cantidadReservada;
  private List<MetodoIntercambio> metodos;
  private String perfilId;
  private String nombreUsuario;
  private Integer reputacion;

  public FiguritaIntercambiableDto(FiguritaIntercambiable f, Perfil perfil) {
    this.figuritaId = f.getFigurita().getId();
    this.numero = f.getFigurita().getNumero();
    this.jugador = f.getFigurita().getJugador();
    this.seleccion = f.getFigurita().getSeleccion();
    this.cantidadExistente = f.getCantidadExistente();
    this.cantidadReservada = f.getCantidadReservada();
    this.metodos = f.getMetodos();
    this.perfilId = f.getPerfilId();
    this.nombreUsuario = perfil != null ? perfil.getNombre() : null;
    this.reputacion = perfil != null ? (int) Math.round(perfil.getCalificacionMedia()) : null;
  }

  public FiguritaIntercambiableDto(FiguritaIntercambiable f) {
    this(f, null);
  }
}