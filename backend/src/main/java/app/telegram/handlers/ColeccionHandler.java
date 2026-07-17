package app.telegram.handlers;

import app.dto.FiguritaDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.filtros.FaltantesFiltro;
import app.dto.filtros.RepetidasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.paginacion.Repetidas;
import app.model.entities.MetodoIntercambio;
import app.servicios.ServicioColeccion;
import app.servicios.ServicioJwt;
import app.telegram.bot.BotResponse;
import app.telegram.sesion.SessionManager;
import app.telegram.utils.MessageBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("!test & !loadtest")
@Order(2)
public class ColeccionHandler implements BotHandler {

  private static final Logger log = LoggerFactory.getLogger(ColeccionHandler.class);

  private final ServicioColeccion coleccionService;
  private final ServicioJwt servicioJwt;
  private final SessionManager sessionManager;
  private final MessageBuilder messageBuilder;

  // Estado del flujo multi-paso por chat
  private final Map<Long, String> estadoPendiente = new ConcurrentHashMap<>();
  // Guardamos datos temporales entre pasos (ej: figId mientras esperamos cantidad)
  private final Map<Long, Map<String, String>> datosPendientes = new ConcurrentHashMap<>();

  public ColeccionHandler(ServicioColeccion coleccionService,
                          ServicioJwt servicioJwt,
                          SessionManager sessionManager,
                          MessageBuilder messageBuilder) {
    this.coleccionService = coleccionService;
    this.servicioJwt = servicioJwt;
    this.sessionManager = sessionManager;
    this.messageBuilder = messageBuilder;
  }

  @Override
  public Set<String> comandos() {
    return Set.of(
        "/coleccion", "/misfaltantes", "/misrepetidas",
        "/agfaltante", "/agrepetida"
    );
  }

  @Override
  public Set<String> callbackPrefijos() {
    return Set.of("faltantes:", "repetidas:");
  }

  @Override
  public BotResponse handle(Update update) {
    String text = update.getMessage().getText();
    return switch (text) {
      case "/coleccion"    -> handleMenu(update);
      case "/misfaltantes" -> handleVerFaltantes(update);
      case "/misrepetidas" -> handleVerRepetidas(update);
      case "/agfaltante"   -> handleAgregarFaltante(update);
      case "/agrepetida"   -> handleAgregarRepetida(update);
      default              -> null;
    };
  }

  @Override
  public BotResponse handleCallback(Update update) { // 🆕
    String data = update.getCallbackQuery().getData();
    if (data.startsWith("faltantes:")) return handlePaginacionFaltantes(update);
    if (data.startsWith("repetidas:")) return handlePaginacionRepetidas(update);
    return null;
  }

  // ─── Menú principal ───────────────────────────────────────────────

  public BotResponse handleMenu(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    return BotResponse.texto("""
                📦 *Mi Colección*
                
                ¿Qué querés hacer?
                
                /misfaltantes    — Ver mis figuritas faltantes
                /misrepetidas    — Ver mis figuritas repetidas
                /agfaltante      — Agregar una faltante
                /agrepetida      — Agregar una repetida
                """);
  }

  // ─── Ver faltantes ────────────────────────────────────────────────

  public BotResponse handleVerFaltantes(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    return buscarFaltantesYArmar(chatId, 0);
  }

  public BotResponse handlePaginacionFaltantes(Update update) {
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    int pagina = Integer.parseInt(update.getCallbackQuery().getData().split(":")[1]);
    return buscarFaltantesYArmar(chatId, pagina);
  }

  private BotResponse buscarFaltantesYArmar(long chatId, int pagina) {
    try {
      String token = sessionManager.getToken(chatId);
      String colId = servicioJwt.getColeccionId(token);

      FaltantesFiltro filtros = new FaltantesFiltro(5, pagina + 1, null);
      PaginaResultado<FiguritaDto> resultado = coleccionService.buscarFaltantes(colId, filtros);

      if (resultado.contenido().isEmpty()) {
        return BotResponse.texto("😕 No tenés figuritas faltantes cargadas.");
      }

      int paginaActual = pagina + 1;
      StringBuilder sb = new StringBuilder("📋 *Mis faltantes*\n");
      sb.append("📄 Página ").append(paginaActual).append(" de ").append(resultado.cantidadDePaginas()).append("\n\n");

      resultado.contenido().forEach(f ->
          sb.append("• #").append(f.getNumero())
              .append(" — ").append(f.getJugador())
              .append(" (").append(f.getSeleccion()).append(")\n")
      );

      if (resultado.cantidadDePaginas() > 1) {
        return BotResponse.conTeclado(sb.toString(),
            messageBuilder.tecladoPaginacion(pagina, resultado.cantidadDePaginas(), "faltantes"));
      }

      return BotResponse.texto(sb.toString());

    } catch (Exception e) {
      log.error("Error al buscar faltantes para chatId {}: {}", chatId, e.getMessage(), e);
      return BotResponse.texto("❌ Error: " + e.getMessage());
    }
  }

  // ─── Ver repetidas ────────────────────────────────────────────────

  public BotResponse handleVerRepetidas(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    return buscarRepetidasYArmar(chatId, 0);
  }

  public BotResponse handlePaginacionRepetidas(Update update) {
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    int pagina = Integer.parseInt(update.getCallbackQuery().getData().split(":")[1]);
    return buscarRepetidasYArmar(chatId, pagina);
  }

  private BotResponse buscarRepetidasYArmar(long chatId, int pagina) {
    try {
      String token = sessionManager.getToken(chatId);
      String colId = servicioJwt.getColeccionId(token);

      RepetidasFiltro filtros = new RepetidasFiltro(null, null, null,5, pagina + 1);
      Repetidas<FiguritaIntercambiableDto> resultado = coleccionService.buscarRepetidas(colId, filtros);

      if (resultado.getData().contenido().isEmpty()) {
        return BotResponse.texto("😕 No tenés figuritas repetidas cargadas.");
      }

      int paginaActual = pagina + 1;
      StringBuilder sb = new StringBuilder("🔁 *Mis repetidas*\n");
      sb.append("📄 Página ").append(paginaActual).append(" de ")
          .append(resultado.getData().cantidadDePaginas()).append("\n\n");

      resultado.getData().contenido().forEach(f -> {
        String metodos = f.getMetodos().stream()
            .map(m -> m == MetodoIntercambio.SUBASTA ? "🏷️ Subasta" : "🔄 Intercambio")
            .collect(Collectors.joining(" · "));

        sb.append("• #").append(f.getNumero())
            .append(" — ").append(f.getJugador())
            .append("\n  📦 Cantidad: ").append(f.getCantidadExistente())
            .append(" | 💱 ").append(metodos).append("\n");
      });

      if (resultado.getData().cantidadDePaginas() > 1) {
        return BotResponse.conTeclado(sb.toString(),
            messageBuilder.tecladoPaginacion(pagina, resultado.getData().cantidadDePaginas(), "repetidas"));
      }

      return BotResponse.texto(sb.toString());

    } catch (Exception e) {
      log.error("Error al buscar repetidas para chatId {}: {}", chatId, e.getMessage(), e);
      return BotResponse.texto("❌ Error: " + e.getMessage());
    }
  }

  // ─── Agregar faltante ─────────────────────────────────────────────

  public BotResponse handleAgregarFaltante(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "faltante:esperando_figId");
    return BotResponse.texto("🃏 Ingresá el ID de la figurita que querés agregar como faltante:");
  }

  // ─── Agregar repetida ─────────────────────────────────────────────

  public BotResponse handleAgregarRepetida(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "repetida:esperando_figId");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    return BotResponse.texto("🃏 Ingresá el ID de la figurita repetida:");
  }

  // ─── Flujo multi-paso ─────────────────────────────────────────────

  // Devuelve null si no hay nada pendiente
  public BotResponse handlePendiente(Update update) {
    long chatId = update.getMessage().getChatId();
    String estado = estadoPendiente.get(chatId);

    if (estado == null) return null;

    String texto = update.getMessage().getText();

    return switch (estado) {

      case "faltante:esperando_figId" -> {
        estadoPendiente.remove(chatId);
        yield confirmarAgregarFaltante(chatId, texto.trim().toUpperCase());
      }

      case "repetida:esperando_figId" -> {
        datosPendientes.get(chatId).put("figId", texto.trim().toUpperCase());
        estadoPendiente.put(chatId, "repetida:esperando_cantidad");
        yield BotResponse.texto("📦 ¿Cuántas unidades repetidas tenés?");
      }

      case "repetida:esperando_cantidad" -> {
        try {
          Integer.parseInt(texto); // validamos que sea número
        } catch (NumberFormatException e) {
          yield BotResponse.texto("❌ Ingresá un número válido:");
        }
        datosPendientes.get(chatId).put("cantidad", texto);
        estadoPendiente.put(chatId, "repetida:esperando_metodos");
        yield BotResponse.texto("""
                        💱 ¿Cómo querés intercambiarla?
                        
                        1 — 🔄 Solo intercambio
                        2 — 🏷️ Solo subasta
                        3 — Ambos
                        """);
      }

      case "repetida:esperando_metodos" -> {
        List<MetodoIntercambio> metodos = parsearMetodos(texto);
        if (metodos == null) {
          yield BotResponse.texto("❌ Opción inválida. Ingresá 1, 2 o 3:");
        }
        datosPendientes.get(chatId).put("metodos", texto);
        estadoPendiente.remove(chatId);
        yield confirmarAgregarRepetida(chatId, metodos);
      }

      default -> {
        estadoPendiente.remove(chatId);
        yield BotResponse.texto("❌ Ocurrió un error. Intentá de nuevo.");
      }
    };
  }

  public boolean tienePendiente(long chatId) {
    return estadoPendiente.containsKey(chatId);
  }

  public void cancelarPendiente(long chatId) {
    estadoPendiente.remove(chatId);
    datosPendientes.remove(chatId);
  }

  // ─── Confirmaciones ───────────────────────────────────────────────

  private BotResponse confirmarAgregarFaltante(long chatId, String figId) {
    try {
      String token = sessionManager.getToken(chatId);
      String colId = servicioJwt.getColeccionId(token);
      coleccionService.agregarFaltante(colId, figId);
      return BotResponse.texto("✅ Figurita agregada a tus faltantes correctamente.");
    } catch (Exception e) {
      log.error("Error al agregar faltante para chatId {}: {}", chatId, e.getMessage(), e);
      return BotResponse.texto("❌ Error al agregar faltante: " + e.getMessage());
    }
  }

  private BotResponse confirmarAgregarRepetida(long chatId, List<MetodoIntercambio> metodos) {
    try {
      String token = sessionManager.getToken(chatId);
      String colId = servicioJwt.getColeccionId(token);
      String perfilId = servicioJwt.getPerfilId(token);

      Map<String, String> datos = datosPendientes.get(chatId);
      String figId = datos.get("figId");
      int cantidad = Integer.parseInt(datos.get("cantidad"));

      datosPendientes.remove(chatId);

      coleccionService.agregarRepetida(colId, perfilId, figId, cantidad, metodos);
      return BotResponse.texto("✅ Figurita repetida agregada correctamente.");
    } catch (Exception e) {
      log.error("Error al agregar repetida para chatId {}: {}", chatId, e.getMessage(), e);
      return BotResponse.texto("❌ Error al agregar repetida: " + e.getMessage());
    }
  }

  // ─── Helpers ──────────────────────────────────────────────────────

  private List<MetodoIntercambio> parsearMetodos(String opcion) {
    return switch (opcion.trim()) {
      case "1" -> List.of(MetodoIntercambio.INTERCAMBIO);
      case "2" -> List.of(MetodoIntercambio.SUBASTA);
      case "3" -> List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA);
      default  -> null;
    };
  }
}
