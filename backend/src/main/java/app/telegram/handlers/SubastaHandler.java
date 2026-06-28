package app.telegram.handlers;

import app.dto.filtros.SubastasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.EditarOfertaRequest;
import app.dto.request.OfertarEnSubastaRequest;
import app.dto.subasta.MiSubastaActivaDto;
import app.dto.subasta.MiSubastaFinalizadaDto;
import app.dto.subasta.SubastaDto;
import app.dto.subasta.SubastaParticipoDto;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSubasta;
import app.telegram.bot.BotResponse;
import app.telegram.sesion.SessionManager;
import app.telegram.utils.MessageBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(2)
public class SubastaHandler implements BotHandler {

  private static final Logger log = LoggerFactory.getLogger(SubastaHandler.class);

  private final ServicioSubasta subastaService;
  private final ServicioJwt servicioJwt;
  private final SessionManager sessionManager;
  private final MessageBuilder messageBuilder;

  private final Map<Long, String> estadoPendiente = new ConcurrentHashMap<>();
  private final Map<Long, Map<String, String>> datosPendientes = new ConcurrentHashMap<>();

  public SubastaHandler(ServicioSubasta subastaService,
                        ServicioJwt servicioJwt,
                        SessionManager sessionManager,
                        MessageBuilder messageBuilder) {
    this.subastaService = subastaService;
    this.servicioJwt = servicioJwt;
    this.sessionManager = sessionManager;
    this.messageBuilder = messageBuilder;
  }

  // ─── Registro de comandos ─────────────────────────────────────────

  @Override
  public Set<String> comandos() {
    return Set.of(
        "/subasta", "/subastas", "/missubastas", "/participadas"
    );
  }

  @Override
  public Set<String> prefijos() {
    return Set.of(
        "/crearsubasta", "/ofertar",
        "/editaroferta", "/cancelaroferta", "/seleccionar",
        "/rechazaroferta", "/cancelarsubasta", "/cerrarsubasta"
    );
  }

  @Override
  public Set<String> callbackPrefijos() {
    return Set.of(
        "subastas_activas:", "subastas_finalizadas:", "subastas_participadas:"
    );
  }

  // ─── Enrutador principal ──────────────────────────────────────────

  @Override
  public BotResponse handle(Update update) {
    String text = update.getMessage().getText();

    if (text.startsWith("/crearsubasta"))    return handleCrearSubasta(update);
    if (text.startsWith("/ofertar"))         return handleOfertar(update);
    if (text.startsWith("/editaroferta"))    return handleEditarOferta(update);
    if (text.startsWith("/cancelaroferta"))  return handleCancelarOferta(update);
    if (text.startsWith("/seleccionar"))     return handleSeleccionar(update);
    if (text.startsWith("/rechazaroferta"))  return handleRechazarOferta(update);
    if (text.startsWith("/cancelarsubasta")) return handleCancelarSubasta(update);
    if (text.startsWith("/cerrarsubasta"))   return handleCerrarSubasta(update);

    return switch (text) {
      case "/subasta"      -> handleVerSubasta(update);
      case "/subastas"     -> handleMenu(update);
      case "/missubastas"  -> handleMisSubastas(update);
      case "/participadas" -> handleParticipadas(update);
      default              -> null;
    };
  }

  @Override
  public BotResponse handleCallback(Update update) {
    String data = update.getCallbackQuery().getData();
    long chatId = update.getCallbackQuery().getMessage().getChatId();

    String[] partes = data.split(":");
    int pagina = Integer.parseInt(partes[1]);

    return switch (partes[0]) {
      case "subastas_activas"      -> buscarSubastasYArmar(chatId, "activas", pagina);
      case "subastas_finalizadas"  -> buscarSubastasYArmar(chatId, "finalizadas", pagina);
      case "subastas_participadas" -> buscarParticipacionesYArmar(chatId, pagina);
      default                      -> null;
    };
  }

  // ─── Menú principal ───────────────────────────────────────────────

  private BotResponse handleMenu(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    return BotResponse.texto("""
            🏷️ *Subastas*
            
            ¿Qué querés hacer?
            
            /subasta              — Ver detalle de una subasta
            /missubastas          — Ver tus subastas activas y finalizadas
            /participadas         — Ver subastas en las que ofertaste
            /crearsubasta         — Crear una nueva subasta
            /ofertar              — Hacer una oferta en una subasta
            /editaroferta         — Modificar una oferta existente
            /cancelaroferta       — Cancelar una oferta
            /seleccionar          — Seleccionar oferta ganadora
            /rechazaroferta       — Rechazar una oferta
            /cancelarsubasta      — Cancelar una subasta
            /cerrarsubasta        — Cerrar una subasta
            """);
  }

  // ─── Ver detalle de subasta (multi-paso) ──────────────────────────

  private BotResponse handleVerSubasta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "subasta:esperando_id_para_ver");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("🔍 *Ver subasta*\n\nIngresá el ID de la subasta:");
  }

  // ─── Mis subastas (activas / finalizadas) ─────────────────────────

  private BotResponse handleMisSubastas(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "subastas:esperando_tipo");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("vista", "propias");

    return BotResponse.texto("""
            📦 *Mis subastas*
            
            ¿Qué subastas querés ver?
            
            1 — 🟢 Activas
            2 — 🏁 Finalizadas
            """);
  }

  // ─── Participadas ─────────────────────────────────────────────────

  private BotResponse handleParticipadas(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    return buscarParticipacionesYArmar(chatId, 0);
  }

  // ─── Crear subasta (multi-paso) ───────────────────────────────────

  private BotResponse handleCrearSubasta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "subasta:esperando_figurita");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("""
            🏷️ *Nueva subasta*
            
            Paso 1/4 — ¿Qué figurita querés subastar?
            Ingresá el ID de la figurita (ej: `ARG-10`):
            """);
  }

  // ─── Ofertar en subasta (multi-paso) ──────────────────────────────

  private BotResponse handleOfertar(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "oferta:esperando_subasta_id");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("accion", "crear");

    return BotResponse.texto("""
            💰 *Nueva oferta*
            
            Paso 1/2 — Ingresá el ID de la subasta:
            """);
  }

  // ─── Editar oferta (multi-paso) ────────────────────────────────────

  private BotResponse handleEditarOferta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "oferta:esperando_subasta_id");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("accion", "editar");

    return BotResponse.texto("""
            ✏️ *Editar oferta*
            
            Paso 1/3 — Ingresá el ID de la subasta:
            """);
  }

  // ─── Cancelar oferta (multi-paso) ─────────────────────────────────

  private BotResponse handleCancelarOferta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "oferta:esperando_subasta_id");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("accion", "cancelar");

    return BotResponse.texto("""
            🚫 *Cancelar oferta*
            
            Paso 1/2 — Ingresá el ID de la subasta:
            """);
  }

  // ─── Seleccionar oferta ganadora (multi-paso) ─────────────────────

  private BotResponse handleSeleccionar(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "gestion:esperando_subasta_id");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("accion", "seleccionar");

    return BotResponse.texto("""
            ✅ *Seleccionar oferta ganadora*
            
            Paso 1/2 — Ingresá el ID de la subasta:
            """);
  }

  // ─── Rechazar oferta (multi-paso) ─────────────────────────────────

  private BotResponse handleRechazarOferta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "gestion:esperando_subasta_id");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());
    datosPendientes.get(chatId).put("accion", "rechazar");

    return BotResponse.texto("""
            ❌ *Rechazar oferta*
            
            Paso 1/2 — Ingresá el ID de la subasta:
            """);
  }

  // ─── Cancelar subasta (multi-paso) ────────────────────────────────

  private BotResponse handleCancelarSubasta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "subasta:esperando_id_para_cancelar");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("🚫 *Cancelar subasta*\n\nIngresá el ID de la subasta a cancelar:");
  }

  // ─── Cerrar subasta (multi-paso) ──────────────────────────────────

  private BotResponse handleCerrarSubasta(Update update) {
    long chatId = update.getMessage().getChatId();

    if (!sessionManager.isAuthenticated(chatId)) {
      return BotResponse.texto("⚠️ Necesitás iniciar sesión primero. Usá /login");
    }

    estadoPendiente.put(chatId, "subasta:esperando_id_para_cerrar");
    datosPendientes.put(chatId, new ConcurrentHashMap<>());

    return BotResponse.texto("🏁 *Cerrar subasta*\n\nIngresá el ID de la subasta a cerrar:");
  }

  // ─── Flujo multi-paso ─────────────────────────────────────────────

  @Override
  public BotResponse handlePendiente(Update update) {
    long chatId = update.getMessage().getChatId();
    String estado = estadoPendiente.get(chatId);
    String texto = update.getMessage().getText().trim();

    if (estado == null) return null;

    return switch (estado) {

      // ── Mis subastas: elegir tipo ──
      case "subastas:esperando_tipo" -> {
        cancelarPendiente(chatId);
        yield switch (texto) {
          case "1" -> buscarSubastasYArmar(chatId, "activas", 0);
          case "2" -> buscarSubastasYArmar(chatId, "finalizadas", 0);
          default  -> BotResponse.texto("❌ Opción inválida. Respondé 1 o 2.");
        };
      }

      // ── Crear subasta ──
      case "subasta:esperando_figurita" -> {
        datosPendientes.get(chatId).put("figuritaId", texto);
        estadoPendiente.put(chatId, "subasta:esperando_duracion");
        yield BotResponse.texto("Paso 2/4 — ¿Cuántas horas durará la subasta? (ej: `24`):");
      }

      case "subasta:esperando_duracion" -> {
        try {
          Integer.parseInt(texto);
        } catch (NumberFormatException e) {
          yield BotResponse.texto("❌ Ingresá un número entero de horas (ej: `24`):");
        }
        datosPendientes.get(chatId).put("duracion", texto);
        estadoPendiente.put(chatId, "subasta:esperando_figuritas_deseadas");
        yield BotResponse.texto("""
                    Paso 3/4 — ¿Qué figuritas aceptarías como oferta?
                    Ingresá los IDs separados por coma (ej: `BRA-10, ESP-7`):
                    """);
      }

      case "subasta:esperando_figuritas_deseadas" -> {
        List<String> ids = Arrays.stream(texto.split(","))
            .map(String::trim).filter(s -> !s.isBlank()).toList();
        if (ids.isEmpty()) {
          yield BotResponse.texto("❌ Ingresá al menos un ID de figurita:");
        }
        datosPendientes.get(chatId).put("figuritasDeseadas", String.join(",", ids));
        estadoPendiente.put(chatId, "subasta:esperando_calificacion");
        yield BotResponse.texto("Paso 4/4 — Calificación mínima requerida del ofertante (0 a 5):");
      }

      case "subasta:esperando_calificacion" -> {
        int cal;
        try {
          cal = Integer.parseInt(texto);
          if (cal < 0 || cal > 5) throw new NumberFormatException();
        } catch (NumberFormatException e) {
          yield BotResponse.texto("❌ Ingresá un número entre 0 y 5:");
        }
        datosPendientes.get(chatId).put("calificacion", texto);
        estadoPendiente.put(chatId, "subasta:confirmando");

        Map<String, String> datos = datosPendientes.get(chatId);
        yield BotResponse.texto("""
                    *Confirmá la subasta:*
                    
                    🃏 Figurita subastada: `%s`
                    ⏱️ Duración: `%s hs`
                    🎁 Figuritas deseadas: `%s`
                    ⭐ Calificación mínima: `%s`
                    
                    ¿Confirmás? Respondé *si* o *no*
                    """.formatted(
            datos.get("figuritaId"),
            datos.get("duracion"),
            datos.get("figuritasDeseadas"),
            datos.get("calificacion")
        ));
      }

      case "subasta:confirmando" -> {
        if (texto.equalsIgnoreCase("si")) {
          yield confirmarCrearSubasta(chatId);
        } else if (texto.equalsIgnoreCase("no")) {
          cancelarPendiente(chatId);
          yield BotResponse.texto("🚫 Subasta cancelada. Usá /crearsubasta para intentar de nuevo.");
        } else {
          yield BotResponse.texto("❓ Respondé *si* o *no*:");
        }
      }

      // ── Ver subasta ──
      case "subasta:esperando_id_para_ver" -> {
        cancelarPendiente(chatId);
        try {
          SubastaDto subasta = subastaService.obtenerSubasta(texto);
          yield BotResponse.texto(formatearSubastaDetalle(subasta));
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error al obtener la subasta: " + e.getMessage());
        }
      }

      // ── Cancelar / cerrar subasta ──
      case "subasta:esperando_id_para_cancelar" -> {
        cancelarPendiente(chatId);
        try {
          String token = sessionManager.getToken(chatId);
          String perfilId = servicioJwt.getPerfilId(token);
          subastaService.cancelarSubasta(perfilId, texto);
          yield BotResponse.texto("🚫 Subasta cancelada correctamente.");
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error al cancelar: " + e.getMessage());
        }
      }

      case "subasta:esperando_id_para_cerrar" -> {
        cancelarPendiente(chatId);
        try {
          String token = sessionManager.getToken(chatId);
          String perfilId = servicioJwt.getPerfilId(token);
          subastaService.cerrarSubasta(perfilId, texto);
          yield BotResponse.texto("🏁 Subasta cerrada correctamente.");
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error al cerrar: " + e.getMessage());
        }
      }

      // ── Ofertar / editar / cancelar oferta ──
      case "oferta:esperando_subasta_id" -> {
        datosPendientes.get(chatId).put("subastaId", texto);
        String accion = datosPendientes.get(chatId).get("accion");

        yield switch (accion) {
          case "cancelar" -> {
            estadoPendiente.put(chatId, "oferta:esperando_oferta_id");
            yield BotResponse.texto("Paso 2/2 — Ingresá el ID de la oferta a cancelar:");
          }
          case "editar" -> {
            estadoPendiente.put(chatId, "oferta:esperando_oferta_id");
            yield BotResponse.texto("Paso 2/3 — Ingresá el ID de la oferta a editar:");
          }
          default -> { // crear
            estadoPendiente.put(chatId, "oferta:esperando_figuritas");
            yield BotResponse.texto("""
                            Paso 2/2 — ¿Qué figuritas querés ofrecer?
                            Ingresá los IDs separados por coma (ej: `BRA-10, ESP-7`):
                            """);
          }
        };
      }

      case "oferta:esperando_oferta_id" -> {
        datosPendientes.get(chatId).put("ofertaId", texto);
        String accion = datosPendientes.get(chatId).get("accion");

        yield switch (accion) {
          case "cancelar" -> {
            Map<String, String> datos = datosPendientes.get(chatId);
            String subastaId = datos != null ? datos.get("subastaId") : null;
            cancelarPendiente(chatId);

            try {
              String token = sessionManager.getToken(chatId);
              String perfilId = servicioJwt.getPerfilId(token);
              subastaService.cancelarOferta(perfilId, subastaId, texto);
              yield BotResponse.texto("🚫 Oferta cancelada correctamente.");
            } catch (Exception e) {
              yield BotResponse.texto("❌ Error al cancelar la oferta: " + e.getMessage());
            }
          }
          default -> {
            estadoPendiente.put(chatId, "oferta:esperando_figuritas");
            yield BotResponse.texto("""
              Paso 3/3 — Ingresá los nuevos IDs de figuritas ofrecidas,
              separados por coma (ej: `BRA-10, ESP-7`):
              """);
          }
        };
      }

      case "oferta:esperando_figuritas" -> {
        List<String> ids = Arrays.stream(texto.split(","))
            .map(String::trim).filter(s -> !s.isBlank()).toList();
        if (ids.isEmpty()) {
          yield BotResponse.texto("❌ Ingresá al menos un ID de figurita:");
        }

        // Necesitamos los datos antes de limpiarlos
        Map<String, String> datos = datosPendientes.get(chatId);
        String accion  = datos != null ? datos.get("accion")    : null;
        String subId   = datos != null ? datos.get("subastaId") : null;
        String ofertId = datos != null ? datos.get("ofertaId")  : null;

        cancelarPendiente(chatId);

        try {
          String token = sessionManager.getToken(chatId);
          String perfilId = servicioJwt.getPerfilId(token);

          if ("editar".equals(accion)) {
            EditarOfertaRequest req = new EditarOfertaRequest();
            req.setFiguritasOfrecidasId(ids);
            subastaService.editarOfertaEnSubasta(perfilId, subId, ofertId, req);
            yield BotResponse.texto("✏️ Oferta editada correctamente.");
          } else {
            subastaService.ofertarEnSubasta(perfilId, subId, ids);
            yield BotResponse.texto("💰 ¡Oferta realizada correctamente!");
          }
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error: " + e.getMessage());
        }
      }

      // ── Seleccionar / rechazar oferta ──
      case "gestion:esperando_subasta_id" -> {
        datosPendientes.get(chatId).put("subastaId", texto);
        estadoPendiente.put(chatId, "gestion:esperando_oferta_id");
        String accion = datosPendientes.get(chatId).get("accion");
        String paso = "seleccionar".equals(accion) ? "Paso 2/2 — Ingresá el ID de la oferta a seleccionar:"
            : "Paso 2/2 — Ingresá el ID de la oferta a rechazar:";
        yield BotResponse.texto(paso);
      }

      case "gestion:esperando_oferta_id" -> {
        Map<String, String> datos = datosPendientes.get(chatId);
        String accion  = datos.get("accion");
        String subId   = datos.get("subastaId");
        cancelarPendiente(chatId);

        try {
          String token = sessionManager.getToken(chatId);
          String perfilId = servicioJwt.getPerfilId(token);

          if ("seleccionar".equals(accion)) {
            subastaService.seleccionarOferta(perfilId, subId, texto);
            yield BotResponse.texto("✅ Oferta seleccionada como ganadora.");
          } else {
            subastaService.rechazarOferta(perfilId, subId, texto);
            yield BotResponse.texto("❌ Oferta rechazada.");
          }
        } catch (Exception e) {
          yield BotResponse.texto("❌ Error: " + e.getMessage());
        }
      }

      default -> {
        cancelarPendiente(chatId);
        yield BotResponse.texto("❌ Ocurrió un error. Intentá de nuevo.");
      }
    };
  }

  @Override
  public boolean tienePendiente(long chatId) {
    return estadoPendiente.containsKey(chatId);
  }

  @Override
  public void cancelarPendiente(long chatId) {
    estadoPendiente.remove(chatId);
    datosPendientes.remove(chatId);
  }

  // ─── Búsqueda y formateo de subastas ─────────────────────────────

  private BotResponse buscarSubastasYArmar(long chatId, String tipo, int pagina) {
    try {
      String token = sessionManager.getToken(chatId);
      String perfilId = servicioJwt.getPerfilId(token);

      String estado = "activas".equals(tipo) ? "ACTIVA" : "FINALIZADA";
      SubastasFiltro filtro = new SubastasFiltro(pagina + 1, 5, perfilId, null, estado);

      PaginaResultado<?> resultado = subastaService.obtenerSubastas(perfilId, filtro);

      if (resultado == null || resultado.contenido().isEmpty()) {
        return BotResponse.texto("😕 No tenés subastas " +
            ("activas".equals(tipo) ? "activas" : "finalizadas") + ".");
      }

      int paginaActual = pagina + 1;
      StringBuilder sb = new StringBuilder();
      sb.append("activas".equals(tipo) ? "🟢" : "🏁")
          .append(" *Subastas ").append(tipo).append("*\n")
          .append("📄 Página ").append(paginaActual).append(" de ")
          .append(resultado.cantidadDePaginas()).append("\n\n");

      resultado.contenido().forEach(obj -> {
        if (obj instanceof MiSubastaActivaDto s)    sb.append(formatearSubastaActiva(s));
        else if (obj instanceof MiSubastaFinalizadaDto s) sb.append(formatearSubastaFinalizada(s));
      });

      String prefijo = "activas".equals(tipo) ? "subastas_activas" : "subastas_finalizadas";

      if (resultado.cantidadDePaginas() > 1) {
        return BotResponse.conTeclado(sb.toString(),
            messageBuilder.tecladoPaginacion(pagina, resultado.cantidadDePaginas(), prefijo));
      }

      return BotResponse.texto(sb.toString());

    } catch (Exception e) {
      log.error("Error al buscar subastas para chatId {}: {}", chatId, e.getMessage(), e);
      return BotResponse.texto("❌ Error: " + e.getMessage());
    }
  }

  private BotResponse buscarParticipacionesYArmar(long chatId, int pagina) {
    try {
      String token = sessionManager.getToken(chatId);
      String perfilId = servicioJwt.getPerfilId(token);

      SubastasFiltro filtro = new SubastasFiltro(pagina + 1, 5, null, perfilId, null);

      PaginaResultado<?> resultado = subastaService.obtenerSubastas(perfilId, filtro);

      if (resultado == null || resultado.contenido().isEmpty()) {
        return BotResponse.texto("😕 No participaste en ninguna subasta todavía.");
      }

      int paginaActual = pagina + 1;
      StringBuilder sb = new StringBuilder();
      sb.append("🤝 *Subastas en las que participaste*\n")
          .append("📄 Página ").append(paginaActual).append(" de ")
          .append(resultado.cantidadDePaginas()).append("\n\n");

      resultado.contenido().forEach(obj -> {
        if (obj instanceof SubastaParticipoDto s) sb.append(formatearParticipacion(s));
      });

      if (resultado.cantidadDePaginas() > 1) {
        return BotResponse.conTeclado(sb.toString(),
            messageBuilder.tecladoPaginacion(pagina, resultado.cantidadDePaginas(), "subastas_participadas"));
      }

      return BotResponse.texto(sb.toString());

    } catch (Exception e) {
      log.error("Error al buscar participaciones para chatId {}: {}", chatId, e.getMessage(), e);
      return BotResponse.texto("❌ Error: " + e.getMessage());
    }
  }

  // ─── Confirmar creación ───────────────────────────────────────────

  private BotResponse confirmarCrearSubasta(long chatId) {
    try {
      String token = sessionManager.getToken(chatId);
      String perfilId = servicioJwt.getPerfilId(token);
      Map<String, String> datos = datosPendientes.get(chatId);

      List<String> deseadas = Arrays.stream(datos.get("figuritasDeseadas").split(","))
          .map(String::trim).toList();

      subastaService.crearSubasta(
          perfilId,
          datos.get("figuritaId"),
          Integer.parseInt(datos.get("duracion")),
          deseadas,
          Integer.parseInt(datos.get("calificacion"))
      );

      cancelarPendiente(chatId);
      return BotResponse.texto("✅ *¡Subasta creada exitosamente!*");

    } catch (Exception e) {
      log.error("Error al crear subasta para chatId {}: {}", chatId, e.getMessage(), e);
      cancelarPendiente(chatId);
      return BotResponse.texto("❌ Error al crear la subasta: " + e.getMessage());
    }
  }

  // ─── Helpers de formateo ──────────────────────────────────────────

  private String formatearSubastaDetalle(SubastaDto s) {
    StringBuilder sb = new StringBuilder();
    sb.append("🏷️ *Subasta* `").append(s.getId()).append("`\n\n");
    sb.append("👤 Autor: ").append(s.getPerfil().getNombre()).append("\n");
    sb.append("🃏 Figurita subastada: #").append(s.getFigurita().getNumero())
        .append(" — ").append(s.getFigurita().getJugador()).append("\n");
    sb.append("⏱️ Cierre: ").append(s.getCierre()).append("\n");
    sb.append("⭐ Calificación mínima: ").append(s.getCalificacionMinimaSolicitada()).append("\n");
    sb.append("🎁 Figuritas deseadas: ").append(
        s.getFiguritasSolicitadas().stream()
            .map(f -> "#" + f.getNumero() + " " + f.getJugador())
            .collect(Collectors.joining(", "))
    ).append("\n\n");

    if (s.getOfertas().isEmpty()) {
      sb.append("📭 Sin ofertas aún.\n");
    } else {
      sb.append("📬 *Ofertas:*\n");
      s.getOfertas().forEach(o -> {
        sb.append("  🆔 `").append(o.getId()).append("`\n");
        sb.append("  👤 ").append(o.getAutor() != null ? o.getAutor().getNombre() : "—").append("\n");
        sb.append("  🎁 ").append(
            o.getFiguritasOfrecidas().stream()
                .map(f -> "#" + f.getNumero() + " " + f.getJugador())
                .collect(Collectors.joining(", "))
        ).append("\n\n");
      });
    }

    return sb.toString();
  }

  private String formatearSubastaActiva(MiSubastaActivaDto s) {
    return "🆔 `%s`\n🃏 Figurita: #%s — %s\n📅 Apertura: %s\n⏱️ Cierre: %s\n🎁 Ofertas activas: %d\n\n"
        .formatted(
            s.getId(),
            s.getFiguritaSubastada().getNumero(),
            s.getFiguritaSubastada().getJugador(),
            formatearFecha(s.getFechaInicio()),
            formatearFecha(s.getFechaCierre()),
            s.getOfertas().size()
        );
  }

  private String formatearSubastaFinalizada(MiSubastaFinalizadaDto s) {
    String ganador = s.getOfertaGanadora() != null
        ? "🏆 Oferta ganadora: `" + s.getOfertaGanadora().getId() + "`"
        : "📭 Sin oferta ganadora";

    String calificacion = s.isYaCalificado() ? "✅ Ya calificaste" : "⭐ Pendiente de calificación";

    return "🆔 `%s`\n🃏 Figurita: #%s — %s\n⏱️ Cierre: %s\n%s\n%s\n\n"
        .formatted(
            s.getId(),
            s.getFiguritaSubastada().getNumero(),
            s.getFiguritaSubastada().getJugador(),
            formatearFecha(s.getFechaCierre()),
            ganador,
            calificacion
        );
  }

  private String formatearParticipacion(SubastaParticipoDto s) {
    String calificacion = s.isYaCalificado() ? "✅ Ya calificaste" : "⭐ Pendiente de calificación";
    return "🆔 `%s`\n👤 Autor: %s\n🃏 Figurita: #%s — %s\n⏱️ Cierre: %s\n%s\n\n"
        .formatted(
            s.getId(),
            s.getAutor().getNombre(),
            s.getFiguritaSubastada().getNumero(),
            s.getFiguritaSubastada().getJugador(),
            formatearFecha(s.getFechaCierre()),
            calificacion
        );
  }

  private String formatearFecha(LocalDateTime fecha) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", new Locale("es", "AR"));
    return fecha.format(formatter);
  }
}