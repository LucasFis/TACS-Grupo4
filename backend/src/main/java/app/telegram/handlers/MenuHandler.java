package app.telegram.handlers;

import app.telegram.bot.BotResponse;
import app.telegram.sesion.SessionManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

@Component
@Order(1) // junto con AuthHandler, pero Spring los diferencia por tipo
public class MenuHandler implements BotHandler {

  @Override
  public Set<String> comandos() {
    return Set.of("/start", "/menu");
  }

  @Override
  public BotResponse handle(Update update) {
    return switch (update.getMessage().getText()) {
      case "/start" -> handleStart();
      case "/menu"  -> handleMenu();
      default       -> null;
    };
  }

  private BotResponse handleStart() {
    return BotResponse.texto("""
                👋 *¡Bienvenido al bot de Figuritas Mundial!*
                
                Con este bot podés gestionar tus figuritas desde Telegram.
                
                Para empezar, iniciá sesión con /login
                """);
  }

  private BotResponse handleMenu() {
    return BotResponse.texto("""
                📋 *Menú principal*
                
                🃏 Figuritas
                /explorar — Ver figuritas intercambiables
                /buscar   — Buscar por nombre o selección
                
                📦 Colección
                /coleccion — Ver y gestionar mi colección
                
                🔄 Intercambios
                /propuestas — Ver y gestionar propuestas
                
                🏷️ Subastas
                /subastas — Ver subastas activas
                
                /logout — Cerrar sesión
                """);
  }
}
