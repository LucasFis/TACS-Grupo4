package app.telegram;

import app.telegram.bot.BotResponse;
import app.telegram.handlers.MenuHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MenuHandlerTest {

  private MenuHandler menuHandler;

  @BeforeEach
  void setUp() {
    menuHandler = new MenuHandler();
  }

  @Test
  void debeRetornarLosComandosQueManeja() {
    Set<String> comandos = menuHandler.comandos();

    assertEquals(Set.of("/start", "/menu"), comandos);
  }

  @Test
  void debeResponderMensajeDeBienvenidaConStart() {
    Update update = crearUpdateConTexto("/start");

    BotResponse response = menuHandler.handle(update);

    assertNotNull(response);
    assertNull(response.teclado());
    assertTrue(response.texto().contains("¡Bienvenido"));
    assertTrue(response.texto().contains("/login"));
  }

  @Test
  void debeResponderMenuPrincipalConMenu() {
    Update update = crearUpdateConTexto("/menu");

    BotResponse response = menuHandler.handle(update);

    assertNotNull(response);
    assertNull(response.teclado());

    String texto = response.texto();
    assertTrue(texto.contains("Menú principal"));
    assertTrue(texto.contains("/explorar"));
    assertTrue(texto.contains("/buscar"));
    assertTrue(texto.contains("/coleccion"));
    assertTrue(texto.contains("/logout"));
  }

  @Test
  void debeRetornarNullConComandoDesconocido() {
    Update update = crearUpdateConTexto("/algo");

    BotResponse response = menuHandler.handle(update);

    assertNull(response);
  }

  private Update crearUpdateConTexto(String texto) {
    Update update = mock(Update.class);
    Message message = mock(Message.class);

    when(update.getMessage()).thenReturn(message);
    when(message.getText()).thenReturn(texto);

    return update;
  }
}
