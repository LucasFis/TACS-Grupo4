package app.telegram;

import app.dto.FiguritaDto;
import app.dto.FiguritaIntercambiableDto;
import app.dto.filtros.FaltantesFiltro;
import app.dto.filtros.RepetidasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.paginacion.Repetidas;
import app.model.entities.MetodoIntercambio;
import app.model.entities.Seleccion;
import app.servicios.ServicioColeccion;
import app.servicios.ServicioJwt;
import app.telegram.bot.BotResponse;
import app.telegram.handlers.ColeccionHandler;
import app.telegram.sesion.SessionManager;
import app.telegram.utils.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ColeccionHandlerTest {

  private ServicioColeccion coleccionService;
  private ServicioJwt servicioJwt;
  private SessionManager sessionManager;
  private MessageBuilder messageBuilder;
  private ColeccionHandler handler;

  private static final long CHAT_ID = 1L;
  private static final String TOKEN   = "token-test";
  private static final String COL_ID  = "col-123";
  private static final String PERFIL_ID = "perfil-456";

  // ─── Setup ────────────────────────────────────────────────────────

  @BeforeEach
  void setUp() {
    coleccionService = mock(ServicioColeccion.class);
    servicioJwt      = mock(ServicioJwt.class);
    sessionManager   = mock(SessionManager.class);
    messageBuilder   = mock(MessageBuilder.class);
    handler = new ColeccionHandler(coleccionService, servicioJwt, sessionManager, messageBuilder);

    // Sesión autenticada por defecto
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(true);
    when(sessionManager.getToken(CHAT_ID)).thenReturn(TOKEN);
    when(servicioJwt.getColeccionId(TOKEN)).thenReturn(COL_ID);
    when(servicioJwt.getPerfilId(TOKEN)).thenReturn(PERFIL_ID);
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

  private PaginaResultado<FiguritaDto> paginaFaltantes(List<FiguritaDto> contenido, int totalPaginas) {
    return new PaginaResultado<>(contenido, contenido.size(), totalPaginas, 1);
  }

  private PaginaResultado<FiguritaIntercambiableDto> paginaRepetidas(List<FiguritaIntercambiableDto> contenido, int totalPaginas) {
    return new PaginaResultado<>(contenido, contenido.size(), totalPaginas, 1);
  }

  private FiguritaDto figuritaDto(int numero, String jugador, Seleccion seleccion) {
    FiguritaDto f = mock(FiguritaDto.class);
    when(f.getNumero()).thenReturn(numero);
    when(f.getJugador()).thenReturn(jugador);
    when(f.getSeleccion()).thenReturn(seleccion);
    return f;
  }

  private FiguritaIntercambiableDto figuritaRepetida(int numero, String jugador, int cantidad, List<MetodoIntercambio> metodos) {
    FiguritaIntercambiableDto f = mock(FiguritaIntercambiableDto.class);
    when(f.getNumero()).thenReturn(numero);
    when(f.getJugador()).thenReturn(jugador);
    when(f.getCantidadExistente()).thenReturn(cantidad);
    when(f.getMetodos()).thenReturn(metodos);
    return f;
  }

  // ─── handleMenu ───────────────────────────────────────────────────

  @Test
  void menu_autenticado_devuelveOpciones() {
    BotResponse r = handler.handleMenu(updateConTexto(CHAT_ID, "/coleccion"));
    assertTrue(r.texto().contains("Mi Colección"));
  }

  @Test
  void menu_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleMenu(updateConTexto(CHAT_ID, "/coleccion"));
    assertTrue(r.texto().contains("/login"));
  }

  // ─── handleVerFaltantes ───────────────────────────────────────────

  @Test
  void verFaltantes_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleVerFaltantes(updateConTexto(CHAT_ID, "/misfaltantes"));
    assertTrue(r.texto().contains("/login"));
  }

  @Test
  void verFaltantes_listaVacia_devuelveMensajeVacio() throws Exception {
    when(coleccionService.buscarFaltantes(eq(COL_ID), any(FaltantesFiltro.class)))
        .thenReturn(paginaFaltantes(List.of(), 0));

    BotResponse r = handler.handleVerFaltantes(updateConTexto(CHAT_ID, "/misfaltantes"));
    assertTrue(r.texto().contains("No tenés figuritas faltantes"));
  }

  @Test
  void verFaltantes_conResultados_muestraFiguritas() throws Exception {
    FiguritaDto figurita = figuritaDto(10, "Messi", Seleccion.ARGENTINA);
    when(coleccionService.buscarFaltantes(eq(COL_ID), any(FaltantesFiltro.class)))
        .thenReturn(paginaFaltantes(List.of(figurita), 1));

    BotResponse r = handler.handleVerFaltantes(updateConTexto(CHAT_ID, "/misfaltantes"));
    assertTrue(r.texto().contains("Messi"));
    assertTrue(r.texto().contains("ARGENTINA"));
  }

  @Test
  void verFaltantes_variasPaginas_devuelveConTeclado() throws Exception {
    FiguritaDto figurita = figuritaDto(10, "Messi", Seleccion.ARGENTINA);
    when(coleccionService.buscarFaltantes(eq(COL_ID), any(FaltantesFiltro.class)))
        .thenReturn(paginaFaltantes(List.of(figurita), 3));
    when(messageBuilder.tecladoPaginacion(anyInt(), anyInt(), eq("faltantes")))
        .thenReturn(mock(org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.class));

    BotResponse r = handler.handleVerFaltantes(updateConTexto(CHAT_ID, "/misfaltantes"));
    assertNotNull(r.teclado());
  }

  @Test
  void verFaltantes_errorDeServicio_devuelveMensajeDeError() throws Exception {
    when(coleccionService.buscarFaltantes(eq(COL_ID), any(FaltantesFiltro.class)))
        .thenThrow(new RuntimeException("fallo de red"));

    BotResponse r = handler.handleVerFaltantes(updateConTexto(CHAT_ID, "/misfaltantes"));
    assertTrue(r.texto().contains("❌"));
  }

  // ─── handleVerRepetidas ───────────────────────────────────────────

  @Test
  void verRepetidas_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleVerRepetidas(updateConTexto(CHAT_ID, "/misrepetidas"));
    assertTrue(r.texto().contains("/login"));
  }

  @Test
  void verRepetidas_listaVacia_devuelveMensajeVacio() throws Exception {
    Repetidas<FiguritaIntercambiableDto> repetidas = mock(Repetidas.class);
    when(repetidas.getData()).thenReturn(paginaRepetidas(List.of(), 0));
    when(coleccionService.buscarRepetidas(eq(COL_ID), any(RepetidasFiltro.class)))
        .thenReturn(repetidas);

    BotResponse r = handler.handleVerRepetidas(updateConTexto(CHAT_ID, "/misrepetidas"));
    assertTrue(r.texto().contains("No tenés figuritas repetidas"));
  }

  @Test
  void verRepetidas_conResultados_muestraFiguritas() throws Exception {
    FiguritaIntercambiableDto fig = figuritaRepetida(7, "Ronaldo", 2, List.of(MetodoIntercambio.INTERCAMBIO));
    Repetidas<FiguritaIntercambiableDto> repetidas = mock(Repetidas.class);
    when(repetidas.getData()).thenReturn(paginaRepetidas(List.of(fig), 1));
    when(coleccionService.buscarRepetidas(eq(COL_ID), any(RepetidasFiltro.class)))
        .thenReturn(repetidas);

    BotResponse r = handler.handleVerRepetidas(updateConTexto(CHAT_ID, "/misrepetidas"));
    assertTrue(r.texto().contains("Ronaldo"));
    assertTrue(r.texto().contains("Intercambio"));
  }

  // ─── handleAgregarFaltante ────────────────────────────────────────

  @Test
  void agregarFaltante_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleAgregarFaltante(updateConTexto(CHAT_ID, "/agfaltante"));
    assertTrue(r.texto().contains("/login"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void agregarFaltante_autenticado_activaFlujoMultipaso() {
    handler.handleAgregarFaltante(updateConTexto(CHAT_ID, "/agfaltante"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void agregarFaltante_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handleAgregarFaltante(updateConTexto(CHAT_ID, "/agfaltante"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));

    verify(coleccionService).agregarFaltante(COL_ID, "ARG-10");
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void agregarFaltante_errorDeServicio_devuelveMensajeDeError() throws Exception {
    doThrow(new RuntimeException("error")).when(coleccionService).agregarFaltante(any(), any());

    handler.handleAgregarFaltante(updateConTexto(CHAT_ID, "/agfaltante"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));

    assertTrue(r.texto().contains("❌"));
  }

  // ─── handleAgregarRepetida ────────────────────────────────────────

  @Test
  void agregarRepetida_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleAgregarRepetida(updateConTexto(CHAT_ID, "/agrepetida"));
    assertTrue(r.texto().contains("/login"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void agregarRepetida_flujoCompleto_intercambio() throws Exception {
    handler.handleAgregarRepetida(updateConTexto(CHAT_ID, "/agrepetida"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));   // figId
    handler.handlePendiente(updateConTexto(CHAT_ID, "2"));        // cantidad
    handler.handlePendiente(updateConTexto(CHAT_ID, "1"));        // método: intercambio

    verify(coleccionService).agregarRepetida(COL_ID, PERFIL_ID, "BRA-7", 2, List.of(MetodoIntercambio.INTERCAMBIO));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void agregarRepetida_flujoCompleto_subasta() throws Exception {
    handler.handleAgregarRepetida(updateConTexto(CHAT_ID, "/agrepetida"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "3"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "2"));        // método: subasta

    verify(coleccionService).agregarRepetida(COL_ID, PERFIL_ID, "BRA-7", 3, List.of(MetodoIntercambio.SUBASTA));
  }

  @Test
  void agregarRepetida_flujoCompleto_ambosMetodos() throws Exception {
    handler.handleAgregarRepetida(updateConTexto(CHAT_ID, "/agrepetida"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ESP-5"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "1"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "3"));        // método: ambos

    verify(coleccionService).agregarRepetida(COL_ID, PERFIL_ID, "ESP-5", 1,
        List.of(MetodoIntercambio.INTERCAMBIO, MetodoIntercambio.SUBASTA));
  }

  @Test
  void agregarRepetida_cantidadNoNumerica_pideDenuevo() {
    handler.handleAgregarRepetida(updateConTexto(CHAT_ID, "/agrepetida"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "abc")); // cantidad inválida

    assertTrue(r.texto().contains("❌"));
    assertTrue(handler.tienePendiente(CHAT_ID)); // sigue esperando
  }

  @Test
  void agregarRepetida_metodoInvalido_pideDenuevo() {
    handler.handleAgregarRepetida(updateConTexto(CHAT_ID, "/agrepetida"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "2"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "9")); // opción inválida

    assertTrue(r.texto().contains("❌"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  // ─── cancelarPendiente ────────────────────────────────────────────

  @Test
  void cancelarPendiente_limpiaElEstado() {
    handler.handleAgregarRepetida(updateConTexto(CHAT_ID, "/agrepetida"));
    assertTrue(handler.tienePendiente(CHAT_ID));

    handler.cancelarPendiente(CHAT_ID);
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  // ─── Paginación por callback ──────────────────────────────────────

  @Test
  void callbackFaltantes_enrutaCorrectamente() throws Exception {
    FiguritaDto figurita = figuritaDto(10, "Messi", Seleccion.ARGENTINA);
    when(coleccionService.buscarFaltantes(eq(COL_ID), any(FaltantesFiltro.class)))
        .thenReturn(paginaFaltantes(List.of(figurita), 1));

    BotResponse r = handler.handleCallback(updateConCallback(CHAT_ID, "faltantes:2"));
    assertTrue(r.texto().contains("Messi"));
  }

  @Test
  void callbackRepetidas_enrutaCorrectamente() throws Exception {
    FiguritaIntercambiableDto fig = figuritaRepetida(7, "Ronaldo", 2, List.of(MetodoIntercambio.SUBASTA));
    Repetidas<FiguritaIntercambiableDto> repetidas = mock(Repetidas.class);
    when(repetidas.getData()).thenReturn(paginaRepetidas(List.of(fig), 1));
    when(coleccionService.buscarRepetidas(eq(COL_ID), any(RepetidasFiltro.class)))
        .thenReturn(repetidas);

    BotResponse r = handler.handleCallback(updateConCallback(CHAT_ID, "repetidas:1"));
    assertTrue(r.texto().contains("Ronaldo"));
  }
}
