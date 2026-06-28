package app.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.AggregationTemporality;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * El agente de OpenTelemetry no exporta los Meters custom de Micrometer de la
 * app (resilience4j, contadores de negocio, etc.), solo su propia
 * instrumentacion (jvm_*, http_server_*, etc.). Este registry exporta esos
 * Meters por OTLP reusando las mismas variables que ya configura el agente
 * para trazas (OTEL_EXPORTER_OTLP_ENDPOINT, OTEL_EXPORTER_OTLP_HEADERS,
 * OTEL_RESOURCE_ATTRIBUTES).
 *
 * <p>Export best-effort: si no hay endpoint configurado (caso local/dev), el bean no se
 * crea y la app arranca igual usando el MeterRegistry de fallback de Spring Boot; las
 * metricas custom quedan en memoria sin exportarse. Se lee siempre de {@code System.getenv}
 * (misma fuente que el agente OTel) para que ambos exporten al mismo destino o ninguno.
 * Devolver {@code null} cuando falta el endpoint tambien suprime la autoconfig OTLP de
 * Spring Boot, que si no apuntaria por defecto a http://localhost:4318.
 */
@Configuration
public class OtlpMetricsConfig {

  @Bean
  @Nullable
  public OtlpMeterRegistry otlpMeterRegistry() {
    String endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
    if (endpoint == null || endpoint.isBlank()) {
      return null;
    }

    Map<String, String> headers = parseKeyValues(System.getenv("OTEL_EXPORTER_OTLP_HEADERS"));
    Map<String, String> resourceAttributes = parseKeyValues(System.getenv("OTEL_RESOURCE_ATTRIBUTES"));
    resourceAttributes.putIfAbsent("service.name",
        System.getenv().getOrDefault("OTEL_SERVICE_NAME", "tacs-backend"));

    String url = construirUrlMetricas(endpoint);

    OtlpConfig config = new OtlpConfig() {
      @Override
      public String get(@NonNull String key) {
        return null;
      }

      @Override
      public @NonNull String url() {
        return url;
      }

      @Override
      public @NonNull Map<String, String> headers() {
        return headers;
      }

      @Override
      public @NonNull Map<String, String> resourceAttributes() {
        return resourceAttributes;
      }

      // Misma temporalidad que el agente OTel (Dockerfile: ..._TEMPORALITY_PREFERENCE=delta)
      // para no mezclar delta (agente) con cumulative (default de Micrometer) en el mismo stack.
      @Override
      public @NonNull AggregationTemporality aggregationTemporality() {
        return AggregationTemporality.DELTA;
      }
    };

    return new OtlpMeterRegistry(config, Clock.SYSTEM);
  }

  /**
   * Arma la URL de métricas tolerando un endpoint con o sin barra final, para no generar
   * un path inválido tipo {@code .../otlp//v1/metrics}.
   */
  static String construirUrlMetricas(String endpoint) {
    String base = endpoint == null ? "" : endpoint.replaceAll("/+$", "");
    return base + "/v1/metrics";
  }

  private static Map<String, String> parseKeyValues(String raw) {
    Map<String, String> result = new HashMap<>();
    if (raw == null || raw.isBlank()) {
      return result;
    }
    for (String par : raw.split(",")) {
      String[] kv = par.split("=", 2);
      if (kv.length == 2) {
        result.put(kv[0].trim(), kv[1].trim());
      }
    }
    return result;
  }
}
