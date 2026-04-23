package app.servicios.impl;

import app.model.entities.*;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioUsuarios;
import app.servicios.IColeccionService;
import app.servicios.INotificacionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColeccionService implements IColeccionService {
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioColecciones repositorioColecciones;
  private final RepositorioUsuarios repositorioUsuarios;
  private final INotificacionService notificacionService;
  //private final Notificador notificador;

  public ColeccionService(RepositorioFiguritas repositorioFiguritas,
                          RepositorioColecciones repositorioColecciones,
                          RepositorioUsuarios repositorioUsuarios,
                          INotificacionService notificacionService
  ) {
    this.repositorioFiguritas = repositorioFiguritas;
    this.repositorioColecciones = repositorioColecciones;
    this.repositorioUsuarios = repositorioUsuarios;
    this.notificacionService = notificacionService;
  }

  public Figurita agregarFaltante(String colId, String figId) {
    Coleccion coleccion = this.repositorioColecciones.buscarPorId(colId);

    Figurita faltante = this.repositorioFiguritas.buscarPorId(figId);

    coleccion.agregarFaltante(faltante);

    repositorioColecciones.guardar(coleccion);

    return faltante;
  }

  public FiguritaIntercambiable agregarRepetida(String colId, String userId, String figId, Integer
      cantidadDisponible, List<String> modosIntercambio) {
    Coleccion coleccion = this.repositorioColecciones.buscarPorId(colId);

    Figurita figurita = this.repositorioFiguritas.buscarPorId(figId);

    FiguritaIntercambiable repetida = new FiguritaIntercambiable(
        figurita, cantidadDisponible, modosIntercambio.stream().map(MetodoIntercambio::fromString)
        .toList(), userId);

    coleccion.agregarRepetida(repetida);
    repositorioColecciones.guardar(coleccion);

    List<Usuario> interesados = this.repositorioUsuarios.buscarPorFiguritaFaltante(repetida
        .getFigurita());

    String cuerpo = "Nueva figurita disponible, Numero: " + repetida.getFigurita().getId() +
        ", Cantidad: " + repetida.getCantidadDisponible();

    this.notificacionService.notificarInteresados(interesados, cuerpo);

    return repetida;
  }
}
