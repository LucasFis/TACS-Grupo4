package app.telegram;

import app.dto.PerfilDto;
import app.dto.paginacion.PaginaResultado;
import app.dto.subasta.MiSubastaActivaDto;
import app.dto.subasta.MiSubastaFinalizadaDto;
import app.dto.subasta.SubastaParticipoDto;
import app.dto.subasta.SubastaDto;
import app.model.entities.Figurita;
import app.servicios.ServicioJwt;
import app.servicios.ServicioSubasta;
import app.telegram.bot.BotResponse;
import app.telegram.handlers.SubastaHandler;
import app.telegram.sesion.SessionManager;
import app.telegram.utils.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubastaHandlerTest {

  private ServicioSubasta subastaService;
  private ServicioJwt servicioJwt;
  private SessionManager sessionManager;
  private MessageBuilder messageBuilder;
  private SubastaHandler handler;

  private static final long CHAT_ID    = 1L;
  private static final String TOKEN    = "token-test";
  private static final String PERFIL_ID = "perfil-123";

  // ─── Setup ────────────────────────────────────────────────────────

  @BeforeEach
  void setUp() {
    subastaService = mock(ServicioSubasta.class);
    servicioJwt    = mock(ServicioJwt.class);
    sessionManager = mock(SessionManager.class);
    messageBuilder = mock(MessageBuilder.class);

    handler = new SubastaHandler(subastaService, servicioJwt, sessionManager, messageBuilder);

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

  private SubastaDto subastaDto(String id) {
    SubastaDto dto = mock(SubastaDto.class);
    when(dto.getId()).thenReturn(id);
    when(dto.getCierre()).thenReturn(LocalDateTime.now().plusDays(1));

    PerfilDto perfil = mock(PerfilDto.class);
    when(perfil.getNombre()).thenReturn("Vendedor");
    when(dto.getPerfil()).thenReturn(perfil);

    Figurita figurita = Figurita.builder().numero(10).jugador("Messi").build();
    when(dto.getFigurita()).thenReturn(figurita);
    when(dto.getFiguritasSolicitadas()).thenReturn(List.of());
    when(dto.getOfertas()).thenReturn(List.of());
    when(dto.getCalificacionMinimaSolicitada()).thenReturn(0);

    return dto;
  }

  private MiSubastaActivaDto subastaActivaDto(String id) {
    MiSubastaActivaDto dto = mock(MiSubastaActivaDto.class);
    when(dto.getId()).thenReturn(id);
    when(dto.getFechaInicio()).thenReturn(LocalDateTime.now().minusDays(1));
    when(dto.getFechaCierre()).thenReturn(LocalDateTime.now().plusDays(1));

    app.dto.FiguritaDto figurita = mock(app.dto.FiguritaDto.class);
    when(figurita.getNumero()).thenReturn(10);
    when(figurita.getJugador()).thenReturn("Messi");
    when(dto.getFiguritaSubastada()).thenReturn(figurita);
    when(dto.getOfertas()).thenReturn(List.of());

    return dto;
  }

  private SubastaParticipoDto subastaParticipoDto(String id) {
    SubastaParticipoDto dto = mock(SubastaParticipoDto.class);
    when(dto.getId()).thenReturn(id);
    when(dto.getFechaInicio()).thenReturn(LocalDateTime.now().minusDays(1));
    when(dto.getFechaCierre()).thenReturn(LocalDateTime.now().plusDays(1));

    PerfilDto perfil = mock(PerfilDto.class);
    when(perfil.getNombre()).thenReturn("Vendedor");
    when(dto.getAutor()).thenReturn(perfil);

    app.dto.FiguritaDto figurita = mock(app.dto.FiguritaDto.class);
    when(figurita.getNumero()).thenReturn(10);
    when(figurita.getJugador()).thenReturn("Messi");
    when(dto.getFiguritaSubastada()).thenReturn(figurita);
    when(dto.isYaCalificado()).thenReturn(false);

    return dto;
  }

  private MiSubastaFinalizadaDto subastaFinalizadaDto(String id) {
    MiSubastaFinalizadaDto dto = mock(MiSubastaFinalizadaDto.class);
    when(dto.getId()).thenReturn(id);
    when(dto.getFechaInicio()).thenReturn(LocalDateTime.now().minusDays(3));
    when(dto.getFechaCierre()).thenReturn(LocalDateTime.now().minusDays(1));

    app.dto.FiguritaDto figurita = mock(app.dto.FiguritaDto.class);
    when(figurita.getNumero()).thenReturn(10);
    when(figurita.getJugador()).thenReturn("Messi");
    when(dto.getFiguritaSubastada()).thenReturn(figurita);
    when(dto.getOfertaGanadora()).thenReturn(null);
    when(dto.isYaCalificado()).thenReturn(false);

    return dto;
  }

  private <T> PaginaResultado<T> paginaVacia() {
    return new PaginaResultado<>(List.of(), 0, 0, 1);
  }

  private <T> PaginaResultado<T> paginaCon(T dto, int totalPaginas) {
    return new PaginaResultado<>(List.of(dto), 1, totalPaginas, 1);
  }

  // ─── handleMenu ───────────────────────────────────────────────────

  @Test
  void menu_autenticado_devuelveOpciones() {
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/subastas"));
    assertTrue(r.texto().contains("Subastas"));
  }

  @Test
  void menu_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/subastas"));
    assertTrue(r.texto().contains("/login"));
  }

  // ─── handleVerSubasta ─────────────────────────────────────────────

  @Test
  void verSubasta_sinId_devuelveInstrucciones() {
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/subasta"));
    assertTrue(r.texto().contains("Ingresá el ID de la subasta"));
  }

  @Test
  void verSubasta_conId_muestraDetalle() throws Exception {
    SubastaDto dto = subastaDto("sub-1");
    when(subastaService.obtenerSubasta("sub-1")).thenReturn(dto);

    handler.handle(updateConTexto(CHAT_ID, "/subasta"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "sub-1"));

    assertTrue(r.texto().contains("sub-1"));
    assertTrue(r.texto().contains("Messi"));
  }

  @Test
  void verSubasta_errorDeServicio_devuelveMensajeError() throws Exception {
    doThrow(new RuntimeException("no encontrada")).when(subastaService).obtenerSubasta(any());
    handler.handle(updateConTexto(CHAT_ID, "/subasta"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "sub-x"));
    assertTrue(r.texto().contains("❌"));
  }

  // ─── handleMisSubastas ────────────────────────────────────────────

  @Test
  void misSubastas_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    assertTrue(r.texto().contains("/login"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void misSubastas_autenticado_activaFlujo() {
    handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void misSubastas_eligeActivas_buscaActivas() throws Exception {
    doReturn(paginaVacia()).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "1"));

    verify(subastaService).obtenerSubastas(eq(PERFIL_ID), any());
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void misSubastas_eligeFinalizadas_buscaFinalizadas() throws Exception {
    doReturn(paginaVacia()).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "2"));

    verify(subastaService).obtenerSubastas(eq(PERFIL_ID), any());
  }

  @Test
  void misSubastas_opcionInvalida_pideDenuevo() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "9"));

    assertTrue(r.texto().contains("❌"));
    // el flujo se cancela (así lo hace el handler con cancelarPendiente antes del switch)
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void misSubastas_listaVacia_devuelveMensajeVacio() throws Exception {
    doReturn(paginaVacia()).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "1"));

    assertTrue(r.texto().contains("No tenés subastas"));
  }

  @Test
  void misSubastas_conResultados_muestraSubastas() throws Exception {
    MiSubastaActivaDto dto = subastaActivaDto("sub-99");
    doReturn(paginaCon(dto, 1)).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "1"));

    assertTrue(r.texto().contains("sub-99"));
  }

  @Test
  void misSubastas_variasPaginas_devuelveConTeclado() throws Exception {
    MiSubastaActivaDto dto = subastaActivaDto("sub-1");
    doReturn(paginaCon(dto, 3)).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());
    when(messageBuilder.tecladoPaginacion(anyInt(), anyInt(), any()))
        .thenReturn(mock(InlineKeyboardMarkup.class));

    handler.handle(updateConTexto(CHAT_ID, "/missubastas"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "1"));

    assertNotNull(r.teclado());
  }

  // ─── handleParticipadas ───────────────────────────────────────────

  @Test
  void participadas_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/participadas"));
    assertTrue(r.texto().contains("/login"));
  }

  @Test
  void participadas_listaVacia_devuelveMensajeVacio() throws Exception {
    doReturn(paginaVacia()).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/participadas"));
    assertTrue(r.texto().contains("No participaste"));
  }

  @Test
  void participadas_conResultados_muestraSubastas() throws Exception {
    SubastaParticipoDto dto = subastaParticipoDto("sub-participada");
    doReturn(paginaCon(dto, 1)).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/participadas"));
    assertTrue(r.texto().contains("sub-participada"));
  }

  // ─── Crear subasta (flujo multi-paso) ────────────────────────────

  @Test
  void crearSubasta_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    assertTrue(r.texto().contains("/login"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearSubasta_autenticado_activaFlujo() {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearSubasta_duracionNoNumerica_pideDenuevo() {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10")); // figurita
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "mucho")); // duración inválida
    assertTrue(r.texto().contains("❌"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearSubasta_calificacionFueraDeRango_pideDenuevo() {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "24"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "9")); // calificación inválida
    assertTrue(r.texto().contains("❌"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearSubasta_figuritasDeseadasVacias_pideDenuevo() {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "24"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, " , , ")); // vacías
    assertTrue(r.texto().contains("❌"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearSubasta_flujoCompleto_creaLaSubasta() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "24"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7, ESP-5"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "3"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "si"));

    verify(subastaService).crearSubasta(eq(PERFIL_ID), eq("ARG-10"), eq(24),
        eq(List.of("BRA-7", "ESP-5")), eq(3));
    assertTrue(r.texto().contains("✅"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearSubasta_confirmacionNegativa_cancelaFlujo() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "24"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "3"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "no"));

    verify(subastaService, never()).crearSubasta(any(), any(), anyInt(), any(), anyInt());
    assertTrue(r.texto().contains("cancelada"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void crearSubasta_confirmacionInvalida_pideDenuevo() {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "ARG-10"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "24"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "3"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "tal vez"));

    assertTrue(r.texto().contains("*si* o *no*"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  // ─── Cancelar / cerrar subasta ────────────────────────────────────

  @Test
  void cancelarSubasta_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/cancelarsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-123"));

    verify(subastaService).cancelarSubasta(PERFIL_ID, "sub-123");
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void cancelarSubasta_errorDeServicio_devuelveMensajeError() throws Exception {
    doThrow(new RuntimeException("no autorizado")).when(subastaService).cancelarSubasta(any(), any());

    handler.handle(updateConTexto(CHAT_ID, "/cancelarsubasta"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, "sub-123"));

    assertTrue(r.texto().contains("❌"));
  }

  @Test
  void cerrarSubasta_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/cerrarsubasta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-123"));

    verify(subastaService).cerrarSubasta(PERFIL_ID, "sub-123");
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  // ─── Ofertar ─────────────────────────────────────────────────────

  @Test
  void ofertar_noAutenticado_pideSesion() {
    when(sessionManager.isAuthenticated(CHAT_ID)).thenReturn(false);
    BotResponse r = handler.handle(updateConTexto(CHAT_ID, "/ofertar"));
    assertTrue(r.texto().contains("/login"));
  }

  @Test
  void ofertar_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/ofertar"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-1"));     // subasta id
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7, ESP-5")); // figuritas

    verify(subastaService).ofertarEnSubasta(eq(PERFIL_ID), eq("sub-1"), eq(List.of("BRA-7", "ESP-5")));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void ofertar_figuritasVacias_pideDenuevo() {
    handler.handle(updateConTexto(CHAT_ID, "/ofertar"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-1"));
    BotResponse r = handler.handlePendiente(updateConTexto(CHAT_ID, " , "));

    assertTrue(r.texto().contains("❌"));
    assertTrue(handler.tienePendiente(CHAT_ID));
  }

  // ─── Editar oferta ────────────────────────────────────────────────

  @Test
  void editarOferta_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/editaroferta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-1"));    // subasta id
    handler.handlePendiente(updateConTexto(CHAT_ID, "oferta-1")); // oferta id
    handler.handlePendiente(updateConTexto(CHAT_ID, "BRA-7"));    // nuevas figuritas

    verify(subastaService).editarOfertaEnSubasta(eq(PERFIL_ID), eq("sub-1"), eq("oferta-1"), any());
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  // ─── Cancelar oferta ─────────────────────────────────────────────

  @Test
  void cancelarOferta_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/cancelaroferta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-1"));    // subasta id
    handler.handlePendiente(updateConTexto(CHAT_ID, "oferta-1")); // oferta id

    verify(subastaService).cancelarOferta(eq(PERFIL_ID), any(), eq("oferta-1"));
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  // ─── Seleccionar / rechazar oferta ───────────────────────────────

  @Test
  void seleccionar_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/seleccionar"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-1"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "oferta-1"));

    verify(subastaService).seleccionarOferta(PERFIL_ID, "sub-1", "oferta-1");
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  @Test
  void rechazarOferta_flujoCompleto_llamaAlServicio() throws Exception {
    handler.handle(updateConTexto(CHAT_ID, "/rechazaroferta"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "sub-1"));
    handler.handlePendiente(updateConTexto(CHAT_ID, "oferta-1"));

    verify(subastaService).rechazarOferta(PERFIL_ID, "sub-1", "oferta-1");
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  // ─── cancelarPendiente ────────────────────────────────────────────

  @Test
  void cancelarPendiente_limpiaElEstado() {
    handler.handle(updateConTexto(CHAT_ID, "/crearsubasta"));
    assertTrue(handler.tienePendiente(CHAT_ID));

    handler.cancelarPendiente(CHAT_ID);
    assertFalse(handler.tienePendiente(CHAT_ID));
  }

  // ─── Callbacks ───────────────────────────────────────────────────

  @Test
  void callbackActivas_enrutaCorrectamente() throws Exception {
    MiSubastaActivaDto dto = subastaActivaDto("sub-activa");
    doReturn(paginaCon(dto, 1)).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    BotResponse r = handler.handleCallback(updateConCallback(CHAT_ID, "subastas_activas:2"));
    assertTrue(r.texto().contains("sub-activa"));
  }

  @Test
  void callbackFinalizadas_enrutaCorrectamente() throws Exception {
    MiSubastaFinalizadaDto dto = subastaFinalizadaDto("sub-final");
    doReturn(paginaCon(dto, 1)).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    BotResponse r = handler.handleCallback(updateConCallback(CHAT_ID, "subastas_finalizadas:1"));
    assertTrue(r.texto().contains("sub-final"));
  }

  @Test
  void callbackParticipadas_enrutaCorrectamente() throws Exception {
    SubastaParticipoDto dto = subastaParticipoDto("sub-participada");
    doReturn(paginaCon(dto, 1)).when(subastaService).obtenerSubastas(eq(PERFIL_ID), any());

    BotResponse r = handler.handleCallback(updateConCallback(CHAT_ID, "subastas_participadas:1"));
    assertTrue(r.texto().contains("sub-participada"));
  }
}