package app.telegram.handlers;

import app.dto.IntercambioDto;
import app.dto.PropuestaDto;
import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearPropuestaRequest;
import app.model.entities.EstadoProceso;
import app.model.entities.Perfil;
import app.repositories.RepositorioPerfiles;
import app.repositories.impl.campos.CamposPerfil;
import app.servicios.ServicioJwt;
import app.servicios.ServicioPropuesta;
import app.telegram.bot.BotResponse;
import app.telegram.sesion.SessionManager;
import app.telegram.utils.MessageBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(2)
public class PropuestaHandler implements BotHandler {

  private static final Logger log = LoggerFactory.getLogger(PropuestaHandler.class);

  private final ServicioPropuesta propuestaService;
  private final ServicioJwt servicioJwt;
  private final SessionManager sessionManager;
  private final MessageBuilder messageBuilder;
  private final RepositorioPerfiles repositorioPerfiles;

  private final Map<Long, String> estadoPendiente = new ConcurrentHashMap<>();
  private final Map<Long, Map<String, String>> datosPendientes = new ConcurrentHashMap<>();
  private final Map<Long, EstadoProceso> ultimoEstado = new ConcurrentHashMap<>();
  private final Map<Long, String> ultimoTipoPropuesta = new ConcurrentHashMap<>();

  public PropuestaHandler(ServicioPropuesta propuestaService,
                          ServicioJwt servicioJwt,
                          SessionManager sessionManager,
                          MessageBuilder messageBuilder,
                          RepositorioPerfiles repositorioPerfiles) {
    this.propuestaService = propuestaService;
    this.servicioJwt = servicioJwt;
    this.sessionManager = sessionManager;
    this.messageBuilder = messageBuilder;
    this.repositorioPerfiles = repositorioPerfiles;
  }

  @Override
  public Set<String> comandos() {
    return Set.of(
        "/propuestas", "/enviadas", "/recibidas", "/proponer",
        "/aceptar", "/rechazar", "/cancelar"
    );
  }

  @Override
  public Set<String> prefijos() {
    return Set.of();
  }

  @Override
  public Set<String> callbackPrefijos() {
    return Set.of("propuestas_enviadas:", "propuestas_recibidas:");
  }

  @Override
  public BotResponse handle(Update update) {
    String text = update.getMessage().getText();

    return switch (text) {
      case "/propuestas" -> this.handleMenu(update);
      case "/enviadas" -> this.handleVerEnviadas(update);
      case "/recibidas" -> this.handleVerRecibidas(update);
      case "/proponer" -> this.handleCrearPropuesta(update);
      case "/aceptar" -> this.handleAceptar(update);
      case "/rechazar" -> this.handleRechazar(update);
      case "/cancelar" -> this.handleCancelar(update);
      default -> null;
    };
  }

  @Override
  public BotResponse handleCallback(Update update) {
    return handlePaginacion(update);
  }

  // ─── Menú principal ───────────────────────────────────────────────

  public BotResponse handleMenu(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    return BotResponse.texto("""
                🔄 *Propuestas de intercambio*
                
                ¿Qué querés hacer?
                
                /enviadas   — Ver propuestas que enviaste
                /recibidas  — Ver propuestas que recibiste
                /proponer   — Crear una nueva propuesta
                /aceptar    — Aceptar una propuesta
                /rechazar   — Rechazar una propuesta
                /cancelar   — Cancelar una propuesta
                """);
  }

  // ─── Ver propuestas enviadas ──────────────────────────────────────

  public BotResponse handleVerEnviadas(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "propuestas:esperando_filtro");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("tipo", "ENVIADAS");

    return BotResponse.texto("""
            📤 *Propuestas enviadas*
            
            ¿Querés filtrar por estado?
            
            1 — ⏳ Pendientes
            2 — ✅ Aceptadas
            3 — ❌ Rechazadas
            4 — 🚫 Canceladas
            0 — Ver todas
            """);
  }

  // ─── Ver propuestas recibidas ─────────────────────────────────────

  public BotResponse handleVerRecibidas(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "propuestas:esperando_filtro");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("tipo", "RECIBIDAS");

    return BotResponse.texto("""
            📥 *Propuestas recibidas*
            
            ¿Querés filtrar por estado?
            
            1 — ⏳ Pendientes
            2 — ✅ Aceptadas
            3 — ❌ Rechazadas
            4 — 🚫 Canceladas
            0 — Ver todas
            """);
  }

  // ─── Paginación ───────────────────────────────────────────────────

  public BotResponse handlePaginacion(Update update) {
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    String data = update.getCallbackQuery().getData();
    String[] partes = data.split(":");
    String tipo = partes[0].equals("propuestas_enviadas") ? "ENVIADAS" : "RECIBIDAS";
    int pagina = Integer.parseInt(partes[1]);

    EstadoProceso estado = ultimoEstado.get(chatId);

    return buscarPropuestasYArmar(chatId, tipo, pagina, estado);
  }

  private BotResponse buscarPropuestasYArmar(long chatId, String tipo, int pagina, EstadoProceso estado) {
    try {
      String token = sessionManager.getToken(chatId);
      String perfilId = servicioJwt.getPerfilId(token);

      PropuestasFiltro filtros = new PropuestasFiltro(tipo, pagina + 1, 5, estado);

      PaginaResultado<IntercambioDto> resultado = propuestaService.buscarPropuestas(perfilId, filtros);

      if (resultado == null || resultado.contenido() == null || resultado.contenido().isEmpty()) {
        return BotResponse.texto("😕 No tenés propuestas " +
            (tipo.equals("ENVIADAS") ? "enviadas" : "recibidas") +
            (estado != null ? " con estado " + formatearEstado(estado) : "") + ".");
      }

      int paginaActual = pagina + 1;
      StringBuilder sb = new StringBuilder();
      sb.append(tipo.equals("ENVIADAS") ? "📤" : "📥")
          .append(" *Propuestas ").append(tipo.equals("ENVIADAS") ? "enviadas" : "recibidas").append("*");

      if (estado != null) {
        sb.append(" — ").append(formatearEstado(estado));
      }

      sb.append("\n📄 Página ").append(paginaActual).append(" de ")
          .append(resultado.cantidadDePaginas()).append("\n\n");

      resultado.contenido().forEach(p -> {
        sb.append("🆔 `").append(p.getId()).append("`\n");
        sb.append("🃏 Buscada: #").append(p.getFiguritaBuscada().getNumero())
            .append(" — ").append(p.getFiguritaBuscada().getJugador()).append("\n");
        sb.append("🎁 Ofrecidas: ");
        sb.append(p.getFiguritasOfrecidas().stream()
            .map(f -> "#" + f.getNumero() + " " + f.getJugador())
            .collect(Collectors.joining(", ")));
        sb.append("\n");
        sb.append("📌 Estado: ").append(formatearEstado(p.getEstado())).append("\n");

        if (tipo.equals("RECIBIDAS") && p.getEstado() == EstadoProceso.PENDIENTE) {
          sb.append("✅ Usá `/aceptar` para aceptar\n");
          sb.append("❌ Usá `/rechazar` para rechazar\n");
        }
        if (tipo.equals("ENVIADAS") && p.getEstado() == EstadoProceso.PENDIENTE) {
          sb.append("🚫 Usá `/cancelar` para cancelar\n");
        }

        sb.append("\n");
      });

      String prefijo = tipo.equals("ENVIADAS") ? "propuestas_enviadas" : "propuestas_recibidas";

      if (resultado.cantidadDePaginas() > 1) {
        return BotResponse.conTeclado(sb.toString(),
            messageBuilder.tecladoPaginacion(pagina, resultado.cantidadDePaginas(), prefijo));
      }

      return BotResponse.texto(sb.toString());

    } catch (Exception e) {
      log.error("Error al buscar propuestas para chatId {}: {}", chatId, e.getMessage(), e);
      return BotResponse.texto("❌ Error: " + e.getMessage());
    }
  }

  // ─── Crear propuesta ──────────────────────────────────────────────

  public BotResponse handleCrearPropuesta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "propuesta:esperando_destinatario");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("""
                🔄 *Nueva propuesta de intercambio*
                
                Paso 1/4 — ¿A quién le querés proponer el intercambio?
                Ingresá el *nombre de usuario* del destinatario:
                """);
  }

  // ─── Aceptar propuesta (multi-paso) ───────────────────────────────

  public BotResponse handleAceptar(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "propuesta:esperando_id_para_aceptar");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("✅ *Aceptar propuesta*\n\nIngresá el ID de la propuesta a aceptar:");
  }

  // ─── Rechazar propuesta (multi-paso) ──────────────────────────────

  public BotResponse handleRechazar(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "propuesta:esperando_id_para_rechazar");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("❌ *Rechazar propuesta*\n\nIngresá el ID de la propuesta a rechazar:");
  }

  // ─── Cancelar propuesta (multi-paso) ──────────────────────────────

  public BotResponse handleCancelar(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "propuesta:esperando_id_para_cancelar");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("🚫 *Cancelar propuesta*\n\nIngresá el ID de la propuesta a cancelar:");
  }

  // ─── Flujo multi-paso ─────────────────────────────────────────────

  public BotResponse handlePendiente(Update update) {
    long chatId = update.getMessage().getChatId();
    String estado = estadoPendiente.get(chatId);
    String texto = update.getMessage().getText().trim();

    if (estado == null) return null;

    return switch (estado) {

      case "propuesta:esperando_destinatario" -> {
        try {
          CamposPerfil sinCampos = new CamposPerfil(false);
          Perfil destinatario = repositorioPerfiles.buscarPorNombre(texto, sinCampos);
          datosPendientes.get(chatId).put("destinatarioId", destinatario.getId());
          datosPendientes.get(chatId).put("destinatarioNombre", destinatario.getNombre()); // 🆕
          estadoPendiente.put(chatId, "propuesta:esperando_figurita_buscada");
          yield BotResponse.texto("""
                ✅ Destinatario encontrado: *%s*
                
                Paso 2/4 — ¿Qué figurita querés pedirle?
                Ingresá el ID de la figurita buscada (ej: `ARG-10`):
                """.formatted(destinatario.getNombre()));
        } catch (Exception e) {
          yield BotResponse.texto("❌ No se encontró ningún usuario con ese nombre. Intentá de nuevo:");
        }
      }

      case "propuesta:esperando_figurita_buscada" -> {
        datosPendientes.get(chatId).put("figuritaBuscadaId", texto);
        estadoPendiente.put(chatId, "propuesta:esperando_figuritas_ofrecidas");
        yield BotResponse.texto("""
                        Paso 3/4 — ¿Qué figuritas querés ofrecer?
                        Ingresá los IDs separados por coma (ej: `BRA-10, ESP-7`):
                        """);
      }

      case "propuesta:esperando_figuritas_ofrecidas" -> {
        List<String> ids = Arrays.stream(texto.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();

        if (ids.isEmpty()) {
          yield BotResponse.texto("❌ Ingresá al menos un ID de figurita:");
        }

        datosPendientes.get(chatId).put("figuritasOfrecidasIds", String.join(",", ids));
        estadoPendiente.put(chatId, "propuesta:confirmando");

        Map<String, String> datos = datosPendientes.get(chatId);
        yield BotResponse.texto("""
                        Paso 4/4 — *Confirmá la propuesta:*
                        
                        👤 Destinatario ID: `%s`
                        🃏 Figurita buscada: `%s`
                        🎁 Figuritas ofrecidas: `%s`
                        
                        ¿Confirmás? Respondé *si* o *no*
                        """.formatted(
            datos.get("destinatarioNombre"),
            datos.get("figuritaBuscadaId"),
            datos.get("figuritasOfrecidasIds")
        ));
      }

      case "propuesta:confirmando" -> {
        if (texto.equalsIgnoreCase("si")) {
          yield confirmarCrearPropuesta(chatId);
        } else if (texto.equalsIgnoreCase("no")) {
          cancelarPendiente(chatId);
          yield BotResponse.texto("🚫 Propuesta cancelada. Usá /proponer para intentar de nuevo.");
        } else {
          yield BotResponse.texto("❓ Respondé *si* o *no*:");
        }
      }

      case "propuesta:esperando_id_para_aceptar" -> {
        cancelarPendiente(chatId);
        try {
          String token = sessionManager.getToken(chatId);
          String perfilId = servicioJwt.getPerfilId(token);
          propuestaService.aceptar(texto, perfilId);
          yield BotResponse.texto("✅ Propuesta aceptada correctamente. ¡El intercambio se realizó!");
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error al aceptar: " + e.getMessage());
        }
      }

      case "propuesta:esperando_id_para_rechazar" -> {
        cancelarPendiente(chatId);
        try {
          String token = sessionManager.getToken(chatId);
          String perfilId = servicioJwt.getPerfilId(token);
          propuestaService.rechazar(texto, perfilId);
          yield BotResponse.texto("❌ Propuesta rechazada.");
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error al rechazar: " + e.getMessage());
        }
      }

      case "propuesta:esperando_id_para_cancelar" -> {
        cancelarPendiente(chatId);
        try {
          String token = sessionManager.getToken(chatId);
          String perfilId = servicioJwt.getPerfilId(token);
          propuestaService.cancelar(texto, perfilId);
          yield BotResponse.texto("🚫 Propuesta cancelada.");
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error al cancelar: " + e.getMessage());
        }
      }

      case "propuestas:esperando_filtro" -> {
        EstadoProceso estadoProceso = switch (texto) {
          case "1" -> EstadoProceso.PENDIENTE;
          case "2" -> EstadoProceso.ACEPTADO;
          case "3" -> EstadoProceso.RECHAZADO;
          case "4" -> EstadoProceso.CANCELADO;
          default -> null;
        };

        String tipo = datosPendientes.get(chatId).get("tipo");
        if (estadoProceso != null) {
          ultimoEstado.put(chatId, estadoProceso);
        } else {
          ultimoEstado.remove(chatId);
        }
        ultimoTipoPropuesta.put(chatId, tipo);

        cancelarPendiente(chatId);
        yield buscarPropuestasYArmar(chatId, tipo, 0, estadoProceso);
      }

      default -> {
        cancelarPendiente(chatId);
        yield BotResponse.texto("❌ Ocurrió un error. Intentá de nuevo con /proponer");
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

  // ─── Helpers ──────────────────────────────────────────────────────

  private BotResponse confirmarCrearPropuesta(long chatId) {
    try {
      String token = sessionManager.getToken(chatId);
      String autorId = servicioJwt.getPerfilId(token);
      Map<String, String> datos = datosPendientes.get(chatId);

      List<String> ofrecidasIds = Arrays.stream(datos.get("figuritasOfrecidasIds").split(","))
          .map(String::trim)
          .toList();

      CrearPropuestaRequest request = new CrearPropuestaRequest(
          datos.get("destinatarioId"),
          datos.get("figuritaBuscadaId"),
          ofrecidasIds
      );

      PropuestaDto propuesta = propuestaService.crearPropuesta(autorId, request);
      cancelarPendiente(chatId);

      return BotResponse.texto("✅ *¡Propuesta creada exitosamente!*\n\n🆔 ID: `" + propuesta.getId() + "`");

    } catch (Exception e) {
      log.error("Error al crear propuesta para chatId {}: {}", chatId, e.getMessage(), e);
      cancelarPendiente(chatId);
      return BotResponse.texto("❌ Error al crear la propuesta: " + e.getMessage());
    }
  }

  private String formatearEstado(EstadoProceso estado) {
    return switch (estado.toString()) {
      case "PENDIENTE"  -> "⏳ Pendiente";
      case "ACEPTADA"   -> "✅ Aceptada";
      case "RECHAZADA"  -> "❌ Rechazada";
      case "CANCELADA"  -> "🚫 Cancelada";
      default           -> estado.toString();
    };
  }
}
