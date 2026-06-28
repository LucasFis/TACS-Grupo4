package app.telegram;

import app.telegram.bot.BotResponse;
import app.telegram.handlers.BotHandler;
import app.telegram.handlers.CommandHandler;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandHandlerTest {

  // ─── Helpers para construir Updates ───────────────────────────────

  private Update updateConTexto(long chatId, String texto) {
    Message message = mock(Message.class);
    when(message.getChatId()).thenReturn(chatId);
    when(message.getText()).thenReturn(texto);
    when(message.hasText()).thenReturn(true);

    Update update = mock(Update.class);
    when(update.hasMessage()).thenReturn(true);
    when(update.hasCallbackQuery()).thenReturn(false);
    when(update.getMessage()).thenReturn(message);
    return update;
  }

  private Update updateConCallback(long chatId, String data) {
    Message message = mock(Message.class);
    when(message.getChatId()).thenReturn(chatId);

    CallbackQuery callback = mock(CallbackQuery.class);
    when(callback.getData()).thenReturn(data);
    when(callback.getMessage()).thenReturn(message);

    Update update = mock(Update.class);
    when(update.hasCallbackQuery()).thenReturn(true);
    when(update.getCallbackQuery()).thenReturn(callback);
    return update;
  }

  private Update updateSinTexto() {
    Update update = mock(Update.class);
    when(update.hasCallbackQuery()).thenReturn(false);
    when(update.hasMessage()).thenReturn(false);
    return update;
  }

  // ─── Handler de prueba ────────────────────────────────────────────

  // Implementación mínima de BotHandler para usar en tests sin Mockito
  // cuando queremos controlar comportamiento exacto
  private BotHandler handlerSimple(Set<String> comandos, BotResponse respuesta) {
    BotHandler h = mock(BotHandler.class);
    when(h.comandos()).thenReturn(comandos);
    when(h.prefijos()).thenReturn(Set.of());
    when(h.callbackPrefijos()).thenReturn(Set.of());
    when(h.handle(any())).thenReturn(respuesta);
    when(h.tienePendiente(anyLong())).thenReturn(false);
    return h;
  }

  // ─── Comando exacto ───────────────────────────────────────────────

  @Test
  void comandoExacto_enrutaAlHandlerCorrecto() {
    BotResponse esperado = BotResponse.texto("ok");
    BotHandler handler = handlerSimple(Set.of("/menu"), esperado);
    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    BotResponse resultado = commandHandler.handle(updateConTexto(1L, "/menu"));

    assertEquals(esperado, resultado);
    verify(handler).handle(any());
  }

  @Test
  void comandoNoReconocido_devuelveMensajeDeError() {
    CommandHandler commandHandler = new CommandHandler(List.of());

    BotResponse resultado = commandHandler.handle(updateConTexto(1L, "/inexistente"));

    assertTrue(resultado.texto().contains("Comando no reconocido"));
  }

  @Test
  void dosHandlers_cadaUnoRespondeASuComando() {
    BotHandler h1 = handlerSimple(Set.of("/inicio"), BotResponse.texto("inicio"));
    BotHandler h2 = handlerSimple(Set.of("/ayuda"), BotResponse.texto("ayuda"));
    CommandHandler commandHandler = new CommandHandler(List.of(h1, h2));

    assertEquals("inicio", commandHandler.handle(updateConTexto(1L, "/inicio")).texto());
    assertEquals("ayuda",  commandHandler.handle(updateConTexto(1L, "/ayuda")).texto());
  }

  // ─── Prefijos ─────────────────────────────────────────────────────

  @Test
  void prefijo_enrutaAlHandlerCorrecto() {
    BotResponse esperado = BotResponse.texto("buscando");
    BotHandler handler = mock(BotHandler.class);
    when(handler.comandos()).thenReturn(Set.of());
    when(handler.prefijos()).thenReturn(Set.of("/buscar"));
    when(handler.callbackPrefijos()).thenReturn(Set.of());
    when(handler.handle(any())).thenReturn(esperado);
    when(handler.tienePendiente(anyLong())).thenReturn(false);

    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    BotResponse resultado = commandHandler.handle(updateConTexto(1L, "/buscar Messi"));

    assertEquals(esperado, resultado);
  }

  @Test
  void prefijo_sinArgumentos_igualEnruta() {
    BotHandler handler = mock(BotHandler.class);
    when(handler.comandos()).thenReturn(Set.of());
    when(handler.prefijos()).thenReturn(Set.of("/buscar"));
    when(handler.callbackPrefijos()).thenReturn(Set.of());
    when(handler.handle(any())).thenReturn(BotResponse.texto("ok"));
    when(handler.tienePendiente(anyLong())).thenReturn(false);

    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    // "/buscar" sin espacio también matchea porque startsWith("/buscar") es true
    assertNotNull(commandHandler.handle(updateConTexto(1L, "/buscar")));
    verify(handler).handle(any());
  }

  // ─── Callbacks ────────────────────────────────────────────────────

  @Test
  void callback_enrutaAlHandlerCorrecto() {
    BotResponse esperado = BotResponse.texto("pagina 2");
    BotHandler handler = mock(BotHandler.class);
    when(handler.comandos()).thenReturn(Set.of());
    when(handler.prefijos()).thenReturn(Set.of());
    when(handler.callbackPrefijos()).thenReturn(Set.of("figuritas:"));
    when(handler.handleCallback(any())).thenReturn(esperado);

    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    BotResponse resultado = commandHandler.handle(updateConCallback(1L, "figuritas:2"));

    assertEquals(esperado, resultado);
    verify(handler).handleCallback(any());
  }

  @Test
  void callback_sinHandlerRegistrado_devuelveNull() {
    CommandHandler commandHandler = new CommandHandler(List.of());

    BotResponse resultado = commandHandler.handle(updateConCallback(1L, "desconocido:1"));

    assertNull(resultado);
  }

  // ─── Update sin mensaje ───────────────────────────────────────────

  @Test
  void updateSinMensaje_devuelveNull() {
    CommandHandler commandHandler = new CommandHandler(List.of());

    BotResponse resultado = commandHandler.handle(updateSinTexto());

    assertNull(resultado);
  }

  // ─── Flujos pendientes ────────────────────────────────────────────

  @Test
  void conPendiente_textoLibre_enrutaAlHandlerPendiente() {
    BotResponse esperado = BotResponse.texto("paso completado");

    BotHandler handler = mock(BotHandler.class);
    when(handler.comandos()).thenReturn(Set.of("/inicio"));
    when(handler.prefijos()).thenReturn(Set.of());
    when(handler.callbackPrefijos()).thenReturn(Set.of());
    when(handler.tienePendiente(1L)).thenReturn(true);
    when(handler.handlePendiente(any())).thenReturn(esperado);

    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    BotResponse resultado = commandHandler.handle(updateConTexto(1L, "texto libre"));

    assertEquals(esperado, resultado);
    verify(handler).handlePendiente(any());
    verify(handler, never()).handle(any());
  }

  @Test
  void conPendiente_llegaUnComando_cancelaElFlujo() {
    BotHandler handler = mock(BotHandler.class);
    when(handler.comandos()).thenReturn(Set.of("/menu"));
    when(handler.prefijos()).thenReturn(Set.of());
    when(handler.callbackPrefijos()).thenReturn(Set.of());
    when(handler.tienePendiente(1L)).thenReturn(true);
    when(handler.handle(any())).thenReturn(BotResponse.texto("menú"));

    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    commandHandler.handle(updateConTexto(1L, "/menu"));

    // Debe cancelar el pendiente y luego enrutar el comando normalmente
    verify(handler).cancelarPendiente(1L);
    verify(handler).handle(any());
  }

  @Test
  void sinPendiente_textoLibre_devuelveMensajeDeError() {
    BotHandler handler = handlerSimple(Set.of("/inicio"), BotResponse.texto("ok"));
    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    BotResponse resultado = commandHandler.handle(updateConTexto(1L, "texto suelto"));

    assertTrue(resultado.texto().contains("Comando no reconocido"));
  }

  @Test
  void pendiente_soloParaEseChatId_noAfectaOtrosChats() {
    BotHandler handler = mock(BotHandler.class);
    when(handler.comandos()).thenReturn(Set.of());
    when(handler.prefijos()).thenReturn(Set.of());
    when(handler.callbackPrefijos()).thenReturn(Set.of());
    when(handler.tienePendiente(1L)).thenReturn(true);
    when(handler.tienePendiente(2L)).thenReturn(false);
    when(handler.handlePendiente(any())).thenReturn(BotResponse.texto("pendiente"));

    CommandHandler commandHandler = new CommandHandler(List.of(handler));

    // Chat 1 tiene pendiente → va a handlePendiente
    commandHandler.handle(updateConTexto(1L, "texto"));
    verify(handler).handlePendiente(any());

    // Chat 2 no tiene pendiente → no llama handlePendiente
    commandHandler.handle(updateConTexto(2L, "texto"));
    verify(handler, times(1)).handlePendiente(any()); // sigue siendo 1 sola vez
  }
}
