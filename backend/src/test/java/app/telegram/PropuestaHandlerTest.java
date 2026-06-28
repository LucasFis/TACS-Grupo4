package app.telegram;

import app.dto.IntercambioDto;
import app.dto.PropuestaDto;
import app.dto.filtros.PropuestasFiltro;
import app.dto.paginacion.PaginaResultado;
import app.dto.request.CrearPropuestaRequest;
import app.model.entities.EstadoProceso;
import app.model.entities.Figurita;
import app.model.entities.Perfil;
import app.repositories.RepositorioPerfiles;
import app.repositories.impl.campos.CamposPerfil;
import app.servicios.ServicioJwt;
import app.servicios.ServicioPropuesta;
import app.telegram.bot.BotResponse;
import app.telegram.handlers.PropuestaHandler;
import app.telegram.sesion.SessionManager;
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

class PropuestaHandlerTest {

  private ServicioPropuesta propuestaService;
  private ServicioJwt servicioJwt;
  private SessionManager sessionManager;
  private MessageBuilder messageBuilder;
  private RepositorioPerfiles repositorioPerfiles;
  private PropuestaHandler handler;

  private static final long CHAT_ID   = 1L;
  private static final String TOKEN    = "token-test";
  private static final String PERFIL_ID = "perfil-123";

  // ─── Setup ────────────────────────────────────────────────────────

  @BeforeEach
  void setUp() {
    propuestaService   = mock(ServicioPropuesta.class);
    servicioJwt        = mock(ServicioJwt.class);
    sessionManager     = mock(SessionManager.class);
    messageBuilder     = mock(MessageBuilder.class);
    repositorioPerfiles = mock(RepositorioPerfiles.class);

    handler = new PropuestaHandler(
        propuestaService, servicioJwt, sessionManager,
        messageBuilder, repositorioPerfiles
    );

    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(true);
    when(sessionManager.getToken(CHAT_ID)).thenReturn(TOKEN);
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

  private PaginaResultado<IntercambioDto> paginaVacia() {
    return new PaginaResultado<>(List.of(), 0, 0, 1);
  }

  private PaginaResultado<IntercambioDto> paginaConPropuesta(IntercambioDto dto, int totalPaginas) {
    return new PaginaResultado<>(List.of(dto), 1, totalPaginas, 1);
  }

  private IntercambioDto intercambioDto(String id, EstadoProceso estado) {
    IntercambioDto dto = mock(IntercambioDto.class);
    when(dto.getId()).thenReturn(id);
    when(dto.getEstado()).thenReturn(estado);

    Figurita figuritaBuscada = Figurita.builder()
        .numero(10)
        .jugador("Messi")
        .build();
    when(dto.getFiguritaBuscada()).thenReturn(figuritaBuscada);
    when(dto.getFiguritasOfrecidas()).thenReturn(List.of());

    return dto;
  }

  // ─── handleMenu ───────────────────────────────────────────────────

  @Test
  void menu_autenticado_devuelveOpciones() {
    BotResponse r = handler.handleMenu(updateConTexto(CHAT_ID, "/propuestas"));
    assertTrue(r.texto().contains("Propuestas de intercambio"));
  }

  @Test
  void menu_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleMenu(updateConTexto(CHAT_ID, "/propuestas"));
    assertTrue(r.texto().contains("/login"));
  }

  // ─── handleVerEnviadas / handleVerRecibidas ───────────────────────

  @Test
  void verEnviadas_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    assertTrue(r.texto().contains("/login"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void verEnviadas_autenticado_activaFlujoFiltro() {
    handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void verRecibidas_autenticado_activaFlujoFiltro() {
    handler.handleVerRecibidas(updateConTexto(CHAT_ID, "/recibidas"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  // ─── Flujo filtro → buscar propuestas ────────────────────────────

  @Test
  void verEnviadas_filtroPendiente_buscaConEstado() throws Exception {
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaVacia());

    handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "1")); // filtro: PENDIENTE

    verify(propuestaService).buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void verEnviadas_filtroTodas_buscaSinEstado() throws Exception {
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaVacia());

    handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "0")); // ver todas

    verify(propuestaService).buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class));
  }

  @Test
  void verEnviadas_sinResultados_devuelveMensajeVacio() throws Exception {
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaVacia());

    handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "0"));

    assertTrue(r.texto().contains("No tenés propuestas"));
  }

  @Test
  void verEnviadas_conResultados_muestraPropuestas() throws Exception {
    IntercambioDto dto = intercambioDto("prop-1", EstadoProceso.PENDIENTE);
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaConPropuesta(dto, 1));

    handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "0"));

    assertTrue(r.texto().contains("prop-1"));
  }

  @Test
  void verEnviadas_propuestaPendiente_muestraOpcionCancelar() throws Exception {
    IntercambioDto dto = intercambioDto("prop-1", EstadoProceso.PENDIENTE);
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaConPropuesta(dto, 1));

    handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "0"));

    assertTrue(r.texto().contains("/cancelar"));
  }

  @Test
  void verRecibidas_propuestaPendiente_muestraOpcionesAceptarRechazar() throws Exception {
    IntercambioDto dto = intercambioDto("prop-1", EstadoProceso.PENDIENTE);
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaConPropuesta(dto, 1));

    handler.handleVerRecibidas(updateConTexto(CHAT_ID, "/recibidas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "0"));

    assertTrue(r.texto().contains("/aceptar"));
    assertTrue(r.texto().contains("/rechazar"));
  }

  @Test
  void verEnviadas_variasPaginas_devuelveConTeclado() throws Exception {
    IntercambioDto dto = intercambioDto("prop-1", EstadoProceso.PENDIENTE);
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaConPropuesta(dto, 3));
    when(messageBuilder.tecladoPaginacion(anyInt(), anyInt(), any()))
        .thenReturn(mock(InlineKeyboardMarkup.class));

    handler.handleVerEnviadas(updateConTexto(CHAT_ID, "/enviadas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "0"));

    assertNotNull(r.teclado());
  }

  // ─── handleAceptar ────────────────────────────────────────────────

  @Test
  void aceptar_sinId_devuelveInstrucciones() {
    BotResponse r = handler.handleAceptar(updateConTexto(CHAT_ID, "/aceptar"));
    assertTrue(r.texto().contains("Ingresá el ID de la propuesta a aceptar"));
  }

  @Test
  void aceptar_conId_llamaAlServicio() throws Exception {
    handler.handleAceptar(updateConTexto(CHAT_ID, "/aceptar"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "prop-123"));
    verify(propuestaService).aceptar("prop-123", PERFIL_ID);
  }

  @Test
  void aceptar_exitoso_devuelveMensajeExito() throws Exception {
    handler.handleAceptar(updateConTexto(CHAT_ID, "/aceptar"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "prop-123"));
    assertTrue(r.texto().contains("✅"));
  }

  @Test
  void aceptar_errorDeServicio_devuelveMensajeDeError() throws Exception {
    doThrow(new RuntimeException("no encontrada")).when(propuestaService).aceptar(any(), any());
    handler.handleAceptar(updateConTexto(CHAT_ID, "/aceptar"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "prop-123"));
    assertTrue(r.texto().contains("❌"));
  }

  // ─── handleRechazar ───────────────────────────────────────────────

  @Test
  void rechazar_sinId_devuelveInstrucciones() {
    BotResponse r = handler.handleRechazar(updateConTexto(CHAT_ID, "/rechazar"));
    assertTrue(r.texto().contains("Ingresá el ID de la propuesta a rechazar"));
  }

  @Test
  void rechazar_conId_llamaAlServicio() throws Exception {
    handler.handleRechazar(updateConTexto(CHAT_ID, "/rechazar"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "prop-123"));
    verify(propuestaService).rechazar("prop-123", PERFIL_ID);
  }

  @Test
  void rechazar_errorDeServicio_devuelveMensajeDeError() throws Exception {
    doThrow(new RuntimeException("error")).when(propuestaService).rechazar(any(), any());
    handler.handleRechazar(updateConTexto(CHAT_ID, "/rechazar"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "prop-123"));
    assertTrue(r.texto().contains("❌"));
  }

  // ─── handleCancelar ───────────────────────────────────────────────

  @Test
  void cancelar_sinId_devuelveInstrucciones() {
    BotResponse r = handler.handleCancelar(updateConTexto(CHAT_ID, "/cancelar"));
    assertTrue(r.texto().contains("Ingresá el ID de la propuesta a cancelar"));
  }

  @Test
  void cancelar_conId_llamaAlServicio() throws Exception {
    handler.handleCancelar(updateConTexto(CHAT_ID, "/cancelar"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "prop-123"));
    verify(propuestaService).cancelar("prop-123", PERFIL_ID);
  }

  // ─── Flujo multi-paso: crear propuesta ───────────────────────────

  @Test
  void crearPropuesta_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    assertTrue(r.texto().contains("/login"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearPropuesta_autenticado_activaFlujo() {
    handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearPropuesta_destinatarioNoEncontrado_pideDenuevo() throws Exception {
    when(repositorioPerfiles.buscarPorNombre(eq("UsuarioX"), any(CamposPerfil.class)))
        .thenThrow(new RuntimeException("no encontrado"));

    handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "UsuarioX"));

    assertTrue(r.texto().contains("No se encontró"));
    assertTrue(handler.tienePendiente(CHAT_ID)); // sigue en el flujo
  }

  @Test
  void crearPropuesta_flujoCompleto_creaLaPropuesta() throws Exception {
    Perfil destinatario = mock(Perfil.class);
    when(destinatario.getId()).thenReturn("dest-456");
    when(destinatario.getNombre()).thenReturn("Juan");
    when(repositorioPerfiles.buscarPorNombre(eq("Juan"), any(CamposPerfil.class)))
        .thenReturn(destinatario);

    PropuestaDto propuestaDto = mock(PropuestaDto.class);
    when(propuestaDto.getId()).thenReturn("nueva-prop-789");
    when(propuestaService.crearPropuesta(eq(PERFIL_ID), any(CrearPropuestaRequest.class)))
        .thenReturn(propuestaDto);

    handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "Juan"));         // destinatario
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));       // figurita buscada
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7, ESP-5")); // figuritas ofrecidas
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "si")); // confirmar

    verify(propuestaService).crearPropuesta(eq(PERFIL_ID), any(CrearPropuestaRequest.class));
    assertTrue(r.texto().contains("nueva-prop-789"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearPropuesta_confirmacionNegativa_cancelaFlujo() throws Exception {
    Perfil destinatario = mock(Perfil.class);
    when(destinatario.getId()).thenReturn("dest-456");
    when(destinatario.getNombre()).thenReturn("Juan");
    when(repositorioPerfiles.buscarPorNombre(eq("Juan"), any(CamposPerfil.class)))
        .thenReturn(destinatario);

    handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "Juan"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "no"));

    verify(propuestaService, never()).crearPropuesta(any(), any());
    assertTrue(r.texto().contains("cancelada"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearPropuesta_figuritasOfrecidasVacias_pideDenuevo() throws Exception {
    Perfil destinatario = mock(Perfil.class);
    when(destinatario.getId()).thenReturn("dest-456");
    when(destinatario.getNombre()).thenReturn("Juan");
    when(repositorioPerfiles.buscarPorNombre(eq("Juan"), any(CamposPerfil.class)))
        .thenReturn(destinatario);

    handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "Juan"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "  ,  ")); // ids vacíos

    assertTrue(r.texto().contains("❌"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearPropuesta_confirmacionInvalida_pideDenuevo() throws Exception {
    Perfil destinatario = mock(Perfil.class);
    when(destinatario.getId()).thenReturn("dest-456");
    when(destinatario.getNombre()).thenReturn("Juan");
    when(repositorioPerfiles.buscarPorNombre(eq("Juan"), any(CamposPerfil.class)))
        .thenReturn(destinatario);

    handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "Juan"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "quizás"));

    assertTrue(r.texto().contains("*si* o *no*"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  // ─── cancelarPendiente ────────────────────────────────────────────

  @Test
  void cancelarPendiente_limpiaElEstado() {
    handler.handleCrearPropuesta(updateConTexto(CHAT_ID, "/proponer"));
    assertTrue(handler.tienePendiente(CHAT_ID));

    handler.cancelarPendiente(CHAT_ID);
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  // ─── handlePaginacion (callback) ─────────────────────────────────

  @Test
  void callbackEnviadas_enrutaCorrectamente() throws Exception {
    IntercambioDto dto = intercambioDto("prop-1", EstadoProceso.PENDIENTE);
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaConPropuesta(dto, 1));

    BotResponse r = handler.handleCallback(updateConCallback(CHAT_ID, "propuestas_enviadas:2"));

    assertTrue(r.texto().contains("prop-1"));
  }

  @Test
  void callbackRecibidas_enrutaCorrectamente() throws Exception {
    IntercambioDto dto = intercambioDto("prop-2", EstadoProceso.PENDIENTE);
    when(propuestaService.buscarPropuestas(eq(PERFIL_ID), any(PropuestasFiltro.class)))
        .thenReturn(paginaConPropuesta(dto, 1));

    BotResponse r = handler.handleCallback(updateConCallback(CHAT_ID, "propuestas_recibidas:1"));

    assertTrue(r.texto().contains("prop-2"));
  }
}