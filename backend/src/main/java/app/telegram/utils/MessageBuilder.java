package app.telegram.utils;

import app.dto.FiguritaIntercambiableDto;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.MetodoIntercambio;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageBuilder {

  // Formatea una figurita individual
  public String formatearRepetida(FiguritaIntercambiableDto f) {
    StringBuilder sb = new StringBuilder();

    sb.append("🃏 *#").append(f.getNumero()).append(" — ").append(f.getJugador()).append("*\n");
    sb.append("🌍 ").append(f.getSeleccion()).append("\n");
    sb.append("📌 ").append(f.getPosicion()).append("\n");

    String metodos = f.getMetodos().stream()
        .map(m -> m == MetodoIntercambio.SUBASTA ? "🏷️ Subasta" : "🔄 Intercambio")
        .collect(Collectors.joining(" · "));
    sb.append("💱 ").append(metodos).append("\n");

    sb.append("📦 Disponibles: ").append(f.getCantidadExistente() - f.getCantidadReservada()).append("\n");

    if (f.getNombreUsuario() != null) {
      sb.append("👤 ").append(f.getNombreUsuario());
      if (f.getReputacion() != null) {
        sb.append(" ⭐ ").append(f.getReputacion());
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  // Formatea una página completa de resultados
  public String formatearPagina(PaginaResultado<FiguritaIntercambiableDto> pagina) {
    if (pagina.contenido().isEmpty()) {
      return "😕 No se encontraron figuritas con esos criterios.";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("🃏 *Figuritas intercambiables*\n");
    sb.append("📄 Página ").append(pagina.numero())
        .append(" de ").append(pagina.cantidadDePaginas())
        .append(" — ").append(pagina.cantidadDeElementos()).append(" resultados\n\n");

    pagina.contenido().forEach(f -> sb.append(formatearRepetida(f)).append("\n"));

    return sb.toString();
  }

  // Teclado de paginación
  public InlineKeyboardMarkup tecladoPaginacion(int paginaActual, int totalPaginas, String prefijo) {
    InlineKeyboardRow fila = new InlineKeyboardRow();

    if (paginaActual > 0) {
      fila.add(InlineKeyboardButton.builder()
          .text("⬅️ Anterior")
          .callbackData(prefijo + ":" + (paginaActual - 1))
          .build());
    }

    if (paginaActual < totalPaginas - 1) {
      fila.add(InlineKeyboardButton.builder()
          .text("Siguiente ➡️")
          .callbackData(prefijo + ":" + (paginaActual + 1))
          .build());
    }

    return InlineKeyboardMarkup.builder()
        .keyboardRow(fila)
        .build();
  }
}
