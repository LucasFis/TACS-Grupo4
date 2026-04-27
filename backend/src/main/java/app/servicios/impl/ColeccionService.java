package app.servicios.impl;

import app.model.entities.*;
import app.repositories.RepositorioColecciones;
import app.repositories.RepositorioFiguritas;
import app.repositories.RepositorioPerfiles;
import app.servicios.IColeccionService;
import app.servicios.INotificacionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColeccionService implements IColeccionService {
  private final RepositorioFiguritas repositorioFiguritas;
  private final RepositorioColecciones repositorioColecciones;
  private final RepositorioPerfiles repositorioUsuarios;
  private final INotificacionService notificacionService;
  //private final Notificador notificador;

  public ColeccionService(RepositorioFiguritas repositorioFiguritas,
                          RepositorioColecciones repositorioColecciones,
                          RepositorioPerfiles repositorioUsuarios,
                          INotificacionService notificacionService
  ) {
    this.repositorioFiguritas = repositorioFiguritas;
    this.repositorioColecciones = repositorioColecciones;
    this.repositorioUsuarios = repositorioUsuarios;
    this.notificacionService = notificacionService;
  }

  public void agregarFaltante(String colId, String figId) {
    Coleccion coleccion = this.repositorioColecciones.buscarPorId(colId);

    Figurita faltante = this.repositorioFiguritas.buscarPorId(figId);

    coleccion.agregarFaltante(faltante);

    repositorioColecciones.guardar(coleccion);
  }

  public void agregarRepetida(String colId, String usuarioId, String figId, Integer
      cantidadExistente, List<String> modosIntercambio) {
    Coleccion coleccion = this.repositorioColecciones.buscarPorId(colId);
    Figurita figurita = this.repositorioFiguritas.buscarPorId(figId);

    Perfil perfil = this.repositorioUsuarios.buscarPorUsuarioId(usuarioId);

    FiguritaIntercambiable repetida = new FiguritaIntercambiable(
        figurita, cantidadExistente, modosIntercambio.stream()
        .map(MetodoIntercambio::fromString)
        .toList(), perfil.getId());

    coleccion.agregarRepetida(repetida);
    repositorioColecciones.guardar(coleccion);

    List<Perfil> interesados = this.repositorioUsuarios.buscarPorFiguritaFaltante(figurita);

    String cuerpo = "Nueva figurita disponible, Numero: " + figurita.getId() +
        ", Cantidad: " + cantidadExistente;

    this.notificacionService.notificarInteresados(interesados, cuerpo);
  }
}
