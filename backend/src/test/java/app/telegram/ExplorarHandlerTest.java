package app.telegram;

import app.dto.FiguritaIntercambiableDto;
import app.dto.paginacion.PaginaResultado;
import app.servicios.ServicioFigurita;
import app.telegram.bot.BotResponse;
import app.telegram.handlers.ExplorarHandler;
import app.telegram.utils.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExplorarHandlerTest {

  private ServicioFigurita figuritaService;
  private MessageBuilder messageBuilder;
  private ExplorarHandler handler;

  private static final long CHAT_ID = 1L;

  // ─── Setup ────────────────────────────────────────────────────────

  @BeforeEach
  void setUp() {
    figuritaService = mock(ServicioFigurita.class);
    messageBuilder  = mock(MessageBuilder.class);
    handler = new ExplorarHandler(figuritaService, messageBuilder);
  }

  // ─── Helpers ──────────────────────────────────────────────────────

  private Update updateConTexto(long chatId, String texto) {
    Message message = mock(Message.class);
    when(message.getChatId()).thenReturn(chatId);
    when(message.getText()).thenReturn(texto);

    Update update = mock(Update.class);
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
    when(update.getCallbackQuery()).thenReturn(callback);
    return update;
  }

  private PaginaResultado<FiguritaIntercambiableDto> pagina(int totalPaginas) {
    return new PaginaResultado<>(List.of(), 0, totalPaginas, 1);
  }

  // ─── handleVerFiguritas ───────────────────────────────────────────

  @Test
  void explorar_llamaABuscarFiguritasSinFiltros() throws Exception {
    when(figuritaService.buscarFiguritas(null, null, null, null, 1, 5)).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("listado");

    handler.handleVerFiguritas(updateConTexto(CHAT_ID, "/explorar"));

    verify(figuritaService).buscarFiguritas(null, null, null, null, 1, 5);
    verify(figuritaService, never()).buscarPorQuery(any(), any(), anyInt(), anyInt());
  }

  @Test
  void explorar_devuelveTextoFormateado() throws Exception {
    when(figuritaService.buscarFiguritas(null, null, null, null, 1, 5)).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("figuritas del mundo");

    BotResponse r = handler.handleVerFiguritas(updateConTexto(CHAT_ID, "/explorar"));

    assertEquals("figuritas del mundo", r.texto());
  }

  @Test
  void explorar_variasPaginas_devuelveConTeclado() throws Exception {
    when(figuritaService.buscarFiguritas(null, null, null, null, 1, 5)).thenReturn(pagina(3));
    when(messageBuilder.formatearPagina(any())).thenReturn("listado");
    when(messageBuilder.tecladoPaginacion(anyInt(), anyInt(), eq("figuritas")))
        .thenReturn(mock(InlineKeyboardMarkup.class));

    BotResponse r = handler.handleVerFiguritas(updateConTexto(CHAT_ID, "/explorar"));

    assertNotNull(r.teclado());
  }

  @Test
  void explorar_unaSolaPagina_sinTeclado() throws Exception {
    when(figuritaService.buscarFiguritas(null, null, null, null, 1, 5)).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("listado");

    BotResponse r = handler.handleVerFiguritas(updateConTexto(CHAT_ID, "/explorar"));

    assertNull(r.teclado());
  }

  @Test
  void explorar_limpiaQueryAnterior() throws Exception {
    // Simula que había una búsqueda previa
    when(figuritaService.buscarPorQuery(eq("Messi"), any(), anyInt(), anyInt())).thenReturn(pagina(1));
    when(figuritaService.buscarFiguritas(null, null, null, null, 1, 5)).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("listado");

    handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Messi"));
    handler.handleVerFiguritas(updateConTexto(CHAT_ID, "/explorar"));

    // La paginación siguiente debe usar buscarFiguritas, no buscarPorQuery
    when(figuritaService.buscarFiguritas(null, null, null, null, 2, 5)).thenReturn(pagina(1));
    handler.handlePaginacion(updateConCallback(CHAT_ID, "figuritas:1"));

    verify(figuritaService, never()).buscarPorQuery(eq("Messi"), any(), eq(2), anyInt());
  }

  // ─── handleBuscar ─────────────────────────────────────────────────

  @Test
  void buscar_sinArgumentos_devuelveInstrucciones() {
    BotResponse r = handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar"));
    assertTrue(r.texto().contains("/buscar Messi"));
  }

  @Test
  void buscar_soloEspacios_devuelveInstrucciones() {
    BotResponse r = handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar   "));
    assertTrue(r.texto().contains("/buscar Messi"));
  }

  @Test
  void buscar_conQuery_llamaABuscarPorQuery() throws Exception {
    when(figuritaService.buscarPorQuery(eq("Messi"), any(), eq(1), eq(5))).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("resultados");

    handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Messi"));

    verify(figuritaService).buscarPorQuery("Messi", null, 1, 5);
    verify(figuritaService, never()).buscarFiguritas(any(), any(), any(), any(), anyInt(), anyInt());
  }

  @Test
  void buscar_conQuery_devuelveTextoFormateado() throws Exception {
    when(figuritaService.buscarPorQuery(eq("Messi"), any(), anyInt(), anyInt())).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("Messi encontrado");

    BotResponse r = handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Messi"));

    assertEquals("Messi encontrado", r.texto());
  }

  @Test
  void buscar_queryConEspacios_pasaQueryCompleta() throws Exception {
    when(figuritaService.buscarPorQuery(eq("Leo Messi"), any(), anyInt(), anyInt())).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("ok");

    handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Leo Messi"));

    verify(figuritaService).buscarPorQuery("Leo Messi", null, 1, 5);
  }

  @Test
  void buscar_variasPaginas_devuelveConTeclado() throws Exception {
    when(figuritaService.buscarPorQuery(eq("Messi"), any(), anyInt(), anyInt())).thenReturn(pagina(3));
    when(messageBuilder.formatearPagina(any())).thenReturn("resultados");
    when(messageBuilder.tecladoPaginacion(anyInt(), anyInt(), eq("figuritas")))
        .thenReturn(mock(InlineKeyboardMarkup.class));

    BotResponse r = handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Messi"));

    assertNotNull(r.teclado());
  }

  @Test
  void buscar_errorDeServicio_devuelveMensajeDeError() throws Exception {
    doThrow(new RuntimeException("timeout"))
        .when(figuritaService).buscarPorQuery(any(), any(), anyInt(), anyInt());

    BotResponse r = handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Messi"));

    assertTrue(r.texto().contains("❌"));
  }

  // ─── handlePaginacion ─────────────────────────────────────────────

  @Test
  void paginacion_sinQueryPrevia_usaBuscarFiguritas() throws Exception {
    when(figuritaService.buscarFiguritas(null, null, null, null, 2, 5)).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("pagina 2");

    handler.handlePaginacion(updateConCallback(CHAT_ID, "figuritas:1"));

    verify(figuritaService).buscarFiguritas(null, null, null, null, 2, 5);
  }

  @Test
  void paginacion_conQueryPrevia_usaBuscarPorQuery() throws Exception {
    // Primero hacemos una búsqueda para guardar la query
    when(figuritaService.buscarPorQuery(eq("Ronaldo"), any(), eq(1), eq(5))).thenReturn(pagina(3));
    when(figuritaService.buscarPorQuery(eq("Ronaldo"), any(), eq(2), eq(5))).thenReturn(pagina(3));
    when(messageBuilder.formatearPagina(any())).thenReturn("ok");

    handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Ronaldo"));
    handler.handlePaginacion(updateConCallback(CHAT_ID, "figuritas:1")); // página 2 (índice 1)

    verify(figuritaService).buscarPorQuery("Ronaldo", null, 2, 5);
  }

  @Test
  void paginacion_numeroDePaginaCorrectoDesdeCallback() throws Exception {
    when(figuritaService.buscarFiguritas(null, null, null, null, 4, 5)).thenReturn(pagina(1));
    when(messageBuilder.formatearPagina(any())).thenReturn("ok");

    // callback "figuritas:3" → pagina = 3 + 1 = 4
    handler.handlePaginacion(updateConCallback(CHAT_ID, "figuritas:3"));

    verify(figuritaService).buscarFiguritas(null, null, null, null, 4, 5);
  }

  @Test
  void paginacion_dosChatsIndependientes() throws Exception {
    long chatId2 = 2L;
    when(figuritaService.buscarPorQuery(eq("Messi"), any(), anyInt(), anyInt())).thenReturn(pagina(2));
    when(figuritaService.buscarFiguritas(isNull(), isNull(), isNull(), isNull(), anyInt(), eq(5)))
        .thenReturn(pagina(3));
    when(messageBuilder.formatearPagina(any())).thenReturn("ok");

    // Chat 1 busca "Messi", chat 2 explora sin query
    handler.handleBuscar(updateConTexto(CHAT_ID, "/buscar Messi"));
    handler.handleVerFiguritas(updateConTexto(chatId2, "/explorar"));

    // Paginación de chat 2 no debe usar la query de chat 1
    Update callbackChat2 = updateConCallback(chatId2, "figuritas:1");
    handler.handlePaginacion(callbackChat2);

    verify(figuritaService).buscarFiguritas(null, null, null, null, 2, 5);
    verify(figuritaService, never()).buscarPorQuery(eq("Messi"), any(), eq(2), anyInt());
  }
}