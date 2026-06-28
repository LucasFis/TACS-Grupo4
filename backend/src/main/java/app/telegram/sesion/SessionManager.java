package app.telegram.sesion;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

  // Guarda chat_id → token JWT en memoria
  // Para producción esto iría en Redis o DB, para el TP alcanza en memoria
  private final Map<Long, String> sessions = new ConcurrentHashMap<>();
  private final Map<Long, String> pendingField = new ConcurrentHashMap<>();

  public void saveToken(long chatId, String token) {
    sessions.put(chatId, token);
  }

  public String getToken(long chatId) {
    return sessions.get(chatId);
  }

  public boolean isAuthenticated(long chatId) {
    return sessions.containsKey(chatId);
  }

  public void logout(long chatId) {
    sessions.remove(chatId);
  }

  // Para flujos multi-paso (ej: esperando que escriba el usuario, luego la contraseña)
  public void setPendingField(long chatId, String field) {
    pendingField.put(chatId, field);
  }

  public String getPendingField(long chatId) {
    return pendingField.get(chatId);
  }

  public void clearPendingField(long chatId) {
    pendingField.remove(chatId);
  }
}