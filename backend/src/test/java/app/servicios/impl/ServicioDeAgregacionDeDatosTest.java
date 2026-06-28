package app.servicios.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.client.ImagenJugadorProveedor;
import app.model.entities.Figurita;
import app.model.entities.Seleccion;
import app.repositories.RepositorioFiguritas;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicioDeAgregacionDeDatosTest {

  @Mock RepositorioFiguritas repositorioFiguritas;
  @Mock ImagenJugadorProveedor imagenProveedor;

  @InjectMocks ServicioDeAgregacionDeDatos service;

  Figurita messi = Figurita.builder()
      .id("ARG-10").numero(10).jugador("Messi")
      .seleccion(Seleccion.ARGENTINA).posicion("Delantero").build();

  @Test
  void enriquecer_figuritaPendiente_llamaApiYGuarda() {
    when(repositorioFiguritas.buscarPendientes(any(), any(), anyInt())).thenReturn(List.of(messi));
    when(repositorioFiguritas.reclamarParaProcesamiento(eq("ARG-10"), any(), any())).thenReturn(messi);
    when(imagenProveedor.buscarImagen("Messi"))
        .thenReturn(Optional.of("https://img.example.com/messi.jpg"));

    service.enriquecer();

    assertEquals("https://img.example.com/messi.jpg", messi.getImagenUrl());
    assertEquals("COMPLETADO", messi.getImagenStatus());
    verify(repositorioFiguritas).guardar(messi);
  }

  @Test
  void enriquecer_proveedorNoEncuentra_guardaConUrlNullYStatusCompletado() {
    when(repositorioFiguritas.buscarPendientes(any(), any(), anyInt())).thenReturn(List.of(messi));
    when(repositorioFiguritas.reclamarParaProcesamiento(eq("ARG-10"), any(), any())).thenReturn(messi);
    when(imagenProveedor.buscarImagen("Messi")).thenReturn(Optional.empty());

    service.enriquecer();

    assertNull(messi.getImagenUrl());
    assertEquals("COMPLETADO", messi.getImagenStatus());
    verify(repositorioFiguritas).guardar(messi);
  }

  @Test
  void enriquecer_otraInstanciaReclamo_seSalta() {
    when(repositorioFiguritas.buscarPendientes(any(), any(), anyInt())).thenReturn(List.of(messi));
    when(repositorioFiguritas.reclamarParaProcesamiento(eq("ARG-10"), any(), any())).thenReturn(null);

    service.enriquecer();

    verify(imagenProveedor, never()).buscarImagen(any());
    verify(repositorioFiguritas, never()).guardar(any());
  }

  @Test
  void enriquecer_excepcionInesperada_continuaConSiguienteFigurita() {
    Figurita mbappe = Figurita.builder()
        .id("FRA-10").numero(10).jugador("Mbappé")
        .seleccion(Seleccion.FRANCIA).posicion("Delantero").build();
    when(repositorioFiguritas.buscarPendientes(any(), any(), anyInt())).thenReturn(List.of(messi, mbappe));
    when(repositorioFiguritas.reclamarParaProcesamiento(eq("ARG-10"), any(), any()))
        .thenThrow(new RuntimeException("error inesperado"));
    when(repositorioFiguritas.reclamarParaProcesamiento(eq("FRA-10"), any(), any())).thenReturn(mbappe);
    when(imagenProveedor.buscarImagen("Mbappé"))
        .thenReturn(Optional.of("https://img.example.com/mbappe.jpg"));

    service.enriquecer();

    assertNull(messi.getImagenUrl());
    assertEquals("https://img.example.com/mbappe.jpg", mbappe.getImagenUrl());
  }

  @Test
  void enriquecer_sinPendientes_noLlamaApi() {
    when(repositorioFiguritas.buscarPendientes(any(), any(), anyInt())).thenReturn(List.of());

    service.enriquecer();

    verify(imagenProveedor, never()).buscarImagen(any());
  }
}
