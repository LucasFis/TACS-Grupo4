package app.client;

import app.client.dto.TheSportsDbPlayerDto;
import app.client.dto.TheSportsDbResponse;
import app.exceptions.RateLimitException;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.text.Normalizer;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class TheSportsDbImagenProveedor implements ImagenJugadorProveedor {

  private static final Logger log = LoggerFactory.getLogger(TheSportsDbImagenProveedor.class);

  private final RestTemplate restTemplate;
  private final RateLimiter rateLimiter;
  private final Retry retry;
  private final String baseUrl;
  private final String apiKey;
  private final MeterRegistry meterRegistry;

  @Autowired
  public TheSportsDbImagenProveedor(
      RestTemplateBuilder builder,
      RateLimiterRegistry rateLimiterRegistry,
      RetryRegistry retryRegistry,
      MeterRegistry meterRegistry,
      @Value("${thesportsdb.base-url}") String baseUrl,
      @Value("${thesportsdb.api-key}") String apiKey,
      @Value("${thesportsdb.rpm:30}") int rpm,
      @Value("${thesportsdb.retry.max-attempts:3}") int maxAttempts,
      @Value("${thesportsdb.retry.wait-seconds:60}") long waitSeconds) {

    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
    this.meterRegistry = meterRegistry;
    this.rateLimiter = crearRateLimiter(rateLimiterRegistry, rpm);
    this.retry = crearRetry(retryRegistry, maxAttempts, waitSeconds);
    this.restTemplate = builder
        .setConnectTimeout(Duration.ofSeconds(3))
        .setReadTimeout(Duration.ofSeconds(3))
        .build();

    TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry).bindTo(meterRegistry);
    TaggedRetryMetrics.ofRetryRegistry(retryRegistry).bindTo(meterRegistry);
  }

  /**
   * Constructor para tests: acepta un RestTemplate mockeado, sin rate limiting ni retry.
   */
  TheSportsDbImagenProveedor(RestTemplate restTemplate, String baseUrl, String apiKey) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
    this.meterRegistry = new SimpleMeterRegistry();
    this.rateLimiter = crearRateLimiter(RateLimiterRegistry.ofDefaults(), Integer.MAX_VALUE);
    this.retry = crearRetry(RetryRegistry.ofDefaults(), 1, 0);
  }

  @Override
  public Optional<String> buscarImagen(String nombreJugador) {
    return Retry.decorateSupplier(retry,
        RateLimiter.decorateSupplier(rateLimiter,
            () -> buscarImagenInterna(nombreJugador))
    ).get();
  }

  /**
   * Realiza la petición HTTP a TheSportsDB para buscar la imagen de un jugador.
   * Normaliza el nombre del jugador para la URL, maneja errores HTTP (incluyendo
   * rate limit 429) y errores inesperados.
   *
   * @param nombreJugador nombre del jugador a buscar
   * @return URL de la imagen, o {@link Optional#empty()} si no se encuentra o hay error
   * @throws app.exceptions.RateLimitException si se recibe un HTTP 429 (demasiadas solicitudes)
   */
  private Optional<String> buscarImagenInterna(String nombreJugador) {
    String url = String.format("%s/%s/searchplayers.php?p=%s", baseUrl, apiKey, normalizarNombre(nombreJugador));

    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      TheSportsDbResponse response = restTemplate.getForObject(url, TheSportsDbResponse.class);
      Optional<String> thumb = extraerThumb(response);
      if (thumb.isPresent()) {
        //log.info("200 OK - imagen encontrada: {}", nombreJugador);
        registrarTiempo(sample, "encontrada");
      } else {
        //log.warn("200 OK - sin imagen: {}", nombreJugador);
        registrarTiempo(sample, "no_encontrada");
      }
      return thumb;
    } catch (HttpClientErrorException e) {
      log.error("{} - {}", e.getStatusCode().value(), nombreJugador);
      if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
        registrarTiempo(sample, "rate_limited");
        throw new RateLimitException("Rate limit alcanzado para TheSportsDB (HTTP 429)");
      }
      registrarTiempo(sample, "error");
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error inesperado buscando imagen de {}: {}", nombreJugador, e.getMessage());
      registrarTiempo(sample, "error");
      return Optional.empty();
    }
  }

  /**
   * Registra la duración de la búsqueda con el desenlace de negocio como tag, sin exponer
   * el nombre del jugador ni la URL con la API key (cardinalidad cerrada en {@code resultado}).
   */
  private void registrarTiempo(Timer.Sample sample, String resultado) {
    sample.stop(meterRegistry.timer("thesportsdb_busqueda_imagen_seconds", "resultado", resultado));
  }

  /**
   * Normaliza el nombre para la URL: NFD descompone cada carácter especial en sus partes
   * ({@code á} → {@code a} + diacrítico), se eliminan los diacríticos y cualquier carácter no
   * alfanumérico (apóstrofes, guiones, etc.), luego minúsculas y espacios a {@code _}.
   *
   * <p>Ej: {@code "Di María"} → {@code "di_maria"}, {@code "N'Golo Kanté"} → {@code "ngolo_kante"}.
   */
  private static String normalizarNombre(String nombre) {
    if (nombre == null) return "";
    String nfd = Normalizer.normalize(nombre, Normalizer.Form.NFD);
    return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .toLowerCase()
        .replaceAll("[^a-z0-9 ]", "")
        .replace(' ', '_');
  }

  private static Optional<String> extraerThumb(TheSportsDbResponse response) {
    if (response == null)
      return Optional.empty();

    List<TheSportsDbPlayerDto> jugadores = response.getPlayer();
    if (jugadores == null || jugadores.isEmpty())
      return Optional.empty();

    String thumb = jugadores.get(0).getStrThumb();
    return (thumb != null && !thumb.isBlank())
        ? Optional.of(thumb)
        : Optional.empty();
  }

  private static RateLimiter crearRateLimiter(RateLimiterRegistry registry, int rpm) {
    // 1 permiso cada (60/rpm) segundos — emite permisos de a uno, espaciados uniformemente.
    // Ej: 30rpm → 1 permiso cada 2s. Evita consumir todos los permisos de golpe.
    long segundosEntreRequests = Math.max(1L, 60L / rpm);
    return registry.rateLimiter("thesportsdb", RateLimiterConfig.custom()
        .limitForPeriod(1)
        .limitRefreshPeriod(Duration.ofSeconds(segundosEntreRequests))
        .timeoutDuration(Duration.ofSeconds(segundosEntreRequests + 1))
        .build());
  }

  private static Retry crearRetry(RetryRegistry registry, int maxAttempts, long waitSeconds) {
    return registry.retry("thesportsdb", RetryConfig.custom()
        .maxAttempts(maxAttempts)
        .waitDuration(Duration.ofSeconds(waitSeconds))
        .retryOnException(e -> e instanceof RateLimitException)
        .build());
  }
}
