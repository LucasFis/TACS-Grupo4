package app.telegram.bot;

import app.telegram.handlers.CommandHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FiguritasBot implements LongPollingUpdateConsumer {

  private static final Logger log = LoggerFactory.getLogger(FiguritasBot.class);

  private final CommandHandler commandHandler;
  private final TelegramClient telegramClient;

  public FiguritasBot(CommandHandler commandHandler,
                      @Value("${telegram.bot.token}") String botToken) {
    this.commandHandler = commandHandler;
    this.telegramClient = new OkHttpTelegramClient(botToken);
  }

  @Override
  public void consume(List<Update> updates) {
    updates.forEach(update -> {
      BotResponse response = commandHandler.handle(update);
      if (response != null) {
        long chatId = getChatId(update);
        enviar(chatId, response);
      }
    });
  }

  private void enviar(long chatId, BotResponse response) {
    SendMessage msg = SendMessage.builder()
        .chatId(chatId)
        .text(response.texto())
        .parseMode("Markdown")
        .replyMarkup(response.teclado()) // null si no hay teclado, está bien
        .build();
    try {
      telegramClient.execute(msg);
    } catch (TelegramApiException e) {
      log.error("Error al enviar mensaje a chatId {}: {}", chatId, e.getMessage(), e);
    }
  }

  private long getChatId(Update update) {
    if (update.hasCallbackQuery()) {
      return update.getCallbackQuery().getMessage().getChatId();
    }
    return update.getMessage().getChatId();
  }
}
