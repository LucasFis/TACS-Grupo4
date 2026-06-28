package app.telegram.handlers;

import app.telegram.bot.BotResponse;
import app.telegram.sesion.SessionManager;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CommandHandler {

  private final Map<String, BotHandler> comandoAHandler = new HashMap<>();
  private final List<Map.Entry<String, BotHandler>> prefijosOrdenados;
  private final Map<String, BotHandler> callbackAHandler = new HashMap<>();
  private final List<BotHandler> handlers;

  public CommandHandler(List<BotHandler> handlers) {
    this.handlers = handlers;

    Map<String, BotHandler> prefijoAHandler = new HashMap<>();

    for (BotHandler handler : handlers) {
      handler.comandos().forEach(cmd -> comandoAHandler.put(cmd, handler));
      handler.prefijos().forEach(pre -> prefijoAHandler.put(pre, handler));
      handler.callbackPrefijos().forEach(cb -> callbackAHandler.put(cb, handler));
    }

    prefijosOrdenados = prefijoAHandler.entrySet().stream()
        .sorted((a, b) -> {
          int cmp = Integer.compare(b.getKey().length(), a.getKey().length());
          return cmp != 0 ? cmp : a.getKey().compareTo(b.getKey());
        })
        .collect(Collectors.toList());
  }

  public BotResponse handle(Update update) {

    if (update.hasCallbackQuery()) return handleCallback(update);
    if (!update.hasMessage() || !update.getMessage().hasText()) return null;

    String text = update.getMessage().getText();
    long chatId = update.getMessage().getChatId();

    // Flujos pendientes
    for (BotHandler handler : handlers) {
      System.out.println(">>> chequeando pendiente: " + handler.getClass().getSimpleName() + " = " + handler.tienePendiente(chatId));
      if (handler.tienePendiente(chatId)) {
        if (text.startsWith("/")) {
          handler.cancelarPendiente(chatId);
          break;
        }
        BotResponse r = handler.handlePendiente(update);
        System.out.println(">>> handlePendiente result: " + r);
        if (r != null) return r;
      }
    }

    // Prefijos con argumentos (ordenados de más largo a más corto)
    for (Map.Entry<String, BotHandler> entry : prefijosOrdenados) {
      if (text.startsWith(entry.getKey())) {
        return entry.getValue().handle(update);
      }
    }

    // Comando exacto
    BotHandler handler = comandoAHandler.get(text);
    if (handler != null) return handler.handle(update);

    return BotResponse.texto("❓ Comando no reconocido. Usá /menu para ver las opciones.");
  }

  private BotResponse handleCallback(Update update) {
    String data = update.getCallbackQuery().getData();

    for (Map.Entry<String, BotHandler> entry : callbackAHandler.entrySet()) {
      if (data.startsWith(entry.getKey())) {
        return entry.getValue().handleCallback(update);
      }
    }

    return null;
  }
}
