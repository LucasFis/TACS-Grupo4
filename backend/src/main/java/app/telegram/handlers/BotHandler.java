package app.telegram.handlers;

import app.telegram.bot.BotResponse;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

// Interfaz que implementa cada handler
public interface BotHandler {
  // Comandos que maneja: "/coleccion", "/misfaltantes", etc.
  Set<String> comandos();

  // Prefijos de comandos con argumentos: "/buscar", "/aceptar"
  default Set<String> prefijos() { return Set.of(); }

  // Procesa el comando
  BotResponse handle(Update update);

  // Si tiene flujo multi-paso en progreso para este chat
  default boolean tienePendiente(long chatId) { return false; }

  // Procesa el paso pendiente
  default BotResponse handlePendiente(Update update) { return null; }

  // Cancela el flujo pendiente
  default void cancelarPendiente(long chatId) {}

  // Prefijos de callbacks que maneja: "figuritas:", "faltantes:"
  default Set<String> callbackPrefijos() { return Set.of(); }

  // Procesa el callback
  default BotResponse handleCallback(Update update) { return null; }
}
