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

  public List<Figurita> buscarFaltantes(String colId) {
    Coleccion coleccion = this.repositorioColecciones.buscarPorId(colId);
    return coleccion.getFaltantes();
  }

  public List<FiguritaIntercambiable> buscarRepetidas(String colId, boolean subasta, boolean intercambio, boolean ambos) {
    Coleccion coleccion = this.repositorioColecciones.buscarPorId(colId);
    List<FiguritaIntercambiable> repetidas = coleccion.getRepetidas();

    if (subasta) {
      repetidas = repetidas.stream()
          .filter(fig -> fig.getMetodos().contains(MetodoIntercambio.SUBASTA)
              || fig.getMetodos().contains(MetodoIntercambio.SUBASTA_E_INTERCAMBIO))
          .toList();
    }

    if (intercambio) {
      repetidas = repetidas.stream()
          .filter(fig -> fig.getMetodos().contains(MetodoIntercambio.INTERCAMBIO)
              || fig.getMetodos().contains(MetodoIntercambio.SUBASTA_E_INTERCAMBIO))
          .toList();
    }

    if (ambos) {
      repetidas = repetidas.stream()
          .filter(fig -> fig.getMetodos().contains(MetodoIntercambio.SUBASTA_E_INTERCAMBIO))
          .toList();
    }

    return repetidas;
  }
}
