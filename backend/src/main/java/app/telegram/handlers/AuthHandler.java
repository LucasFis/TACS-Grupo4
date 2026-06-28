package app.telegram.handlers;

import app.dto.request.LoginRequest;
import app.servicios.ServicioSesion;
import app.telegram.bot.BotResponse;
import app.telegram.sesion.SessionManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class AuthHandler implements BotHandler {

  private final ServicioSesion servicioSesion;
  private final SessionManager sessionManager;

  // Guardamos temporalmente el nombre mientras esperamos la contraseña
  private final Map<Long, String> pendingUsername = new ConcurrentHashMap<>();

  public AuthHandler(ServicioSesion servicioSesion, SessionManager sessionManager) {
    this.servicioSesion = servicioSesion;
    this.sessionManager = sessionManager;
  }

  @Override
  public Set<String> comandos() {
    return Set.of(
        "/login", "/logout"
    );
  }

  @Override
  public BotResponse handle(Update update) {
    String text = update.getMessage().getText();
    return switch (text) {
      case "/login" -> this.handleLogin(update);
      case "/logout" -> this.handleLogout(update);
      default -> null;
    };
  }

  @Override
  public boolean tienePendiente(long chatId) { // 🆕
    return sessionManager.getPendingField(chatId) != null;
  }

  @Override
  public BotResponse handlePendiente(Update update) { // 🆕
    return handlePendingLogin(update);
  }

  @Override
  public void cancelarPendiente(long chatId) { // 🆕
    cancelarLogin(chatId);
  }

  public BotResponse handleLogin(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!update.getMessage().getChat().getType().equals("private")) {
      return BotResponse.texto("⚠️ Por seguridad, el login solo está disponible en chat privado.");
    }

    if (sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("✅ Ya estás autenticado. Usá /menu para ver las opciones.");
    }

    sessionManager.setPendingField(chatId, "username");
    return BotResponse.texto("🔐 *Inicio de sesión*\n\nEscribí tu nombre de usuario:");
  }

  public BotResponse handlePendingLogin(Update update) {
    long chatId = update.getMessage().getChatId();
    String field = sessionManager.getPendingField(chatId);

    if (field == null) return null;

    String text = update.getMessage().getText();

    if (field.equals("username")) {
      pendingUsername.put(chatId, text);
      sessionManager.setPendingField(chatId, "password");
      return BotResponse.texto("🔑 Ahora escribí tu contraseña:");
    }

    if (field.equals("password")) {
      String nombre = pendingUsername.get(chatId);

      try {
        String token = servicioSesion.login(new LoginRequest(nombre, text));
        sessionManager.clearPendingField(chatId);
        pendingUsername.remove(chatId);
        sessionManager.saveToken(chatId, token);
        return BotResponse.texto("✅ *¡Bienvenido, " + nombre + "!*\n\nUsá /menu para ver todas las opciones.");
      } catch (Exception e) {
        cancelarLogin(chatId);
        return BotResponse.texto("❌ Credenciales inválidas. Intentá de nuevo con /login");
      }
    }

    return null;
  }

  public void cancelarLogin(long chatId) {
    sessionManager.clearPendingField(chatId);
    pendingUsername.remove(chatId);
  }

  public BotResponse handleLogout(Update update) {
    sessionManager.logout(update.getMessage().getChatId());
    return BotResponse.texto("👋 Sesión cerrada correctamente.");
  }
}
