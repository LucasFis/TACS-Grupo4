package app.telegram.handlers;

import app.dto.FiguritaIntercambiableDto;
import app.dto.paginacion.PaginaResultado;
import app.model.entities.MetodoIntercambio;
import app.servicios.ServicioFigurita;
import app.telegram.bot.BotResponse;
import app.telegram.utils.MessageBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("!test & !loadtest")
@Order(2)
public class ExplorarHandler implements BotHandler {

  private static final Logger log = LoggerFactory.getLogger(ExplorarHandler.class);

  private final ServicioFigurita figuitaService;
  private final MessageBuilder messageBuilder;

  private final Map<Long, String> ultimaQuery = new ConcurrentHashMap<>();
  private final Map<Long, List<MetodoIntercambio>> ultimoTipo = new ConcurrentHashMap<>();

  public ExplorarHandler(ServicioFigurita figuitaService,
                         MessageBuilder messageBuilder
  ) {
    this.figuitaService = figuitaService;
    this.messageBuilder = messageBuilder;
  }

  @Override
  public Set<String> comandos() {
    return Set.of("/explorar");
  }

  @Override
  public Set<String> prefijos() {
    return Set.of("/buscar");
  }

  @Override
  public Set<String> callbackPrefijos() {
    return Set.of("figuritas:");
  }

  @Override
  public BotResponse handle(Update update) {
    String text = update.getMessage().getText();
    if (text.startsWith("/buscar")) return handleBuscar(update);
    return handleVerFiguritas(update);
  }

  @Override
  public BotResponse handleCallback(Update update) {
    return handlePaginacion(update);
  }

  public BotResponse handleVerFiguritas(Update update) {
    long chatId = update.getMessage().getChatId();
    ultimaQuery.remove(chatId);
    return buscarYArmar(null, 0);
  }

  public BotResponse handleBuscar(Update update) {
    long chatId = update.getMessage().getChatId();
    String[] partes = update.getMessage().getText().split(" ", 2);

    if (partes.length < 2 || partes[1].isBlank()) {
      return BotResponse.texto("🔍 Usá el comando así:\n`/buscar Messi`\n`/buscar Argentina`");
    }

    String query = partes[1].trim();
    ultimaQuery.put(chatId, query);
    return buscarYArmar(query, 0);
  }

  public BotResponse handlePaginacion(Update update) {
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    String data = update.getCallbackQuery().getData();
    int pagina = Integer.parseInt(data.split(":")[1]);

    String query = ultimaQuery.get(chatId);
    return buscarYArmar(query, pagina);
  }

  private BotResponse buscarYArmar(String query, int pagina) {
    try {
      int paginaSegura = pagina + 1;

      PaginaResultado<FiguritaIntercambiableDto> resultado = (query != null && !query.isBlank())
          ? figuitaService.buscarPorQuery(query, null, paginaSegura, 5)
          : figuitaService.buscarFiguritas(null, null, null, null, paginaSegura, 5);

      String texto = messageBuilder.formatearPagina(resultado);

      if (resultado.cantidadDePaginas() > 1) {
        return BotResponse.conTeclado(texto,
            messageBuilder.tecladoPaginacion(pagina, resultado.cantidadDePaginas(), "figuritas"));
      }

      return BotResponse.texto(texto);

    } catch (Exception e) {
      log.error("Error al explorar figuritas: {}", e.getMessage(), e);
      return BotResponse.texto("❌ Error al obtener las figuritas. Intentá de nuevo." + e.getMessage());
    }
  }
}
