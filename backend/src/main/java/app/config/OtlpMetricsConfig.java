package app.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.AggregationTemporality;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

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
 * <p>Export best-effort: si no hay endpoint configurado (caso local/dev y tests), la
 * {@link EndpointOtlpPresente condicion} no se cumple y el bean no se define, con lo que
 * queda el MeterRegistry de fallback de Spring Boot (la app arranca igual, sin exportar).
 * La condicion y la lectura usan la misma fuente ({@code System.getenv}) — la misma que el
 * agente OTel — para evitar asimetrias entre "se decide crear el bean" y "se lee el valor".
 */
@Configuration
@Conditional(OtlpMetricsConfig.EndpointOtlpPresente.class)
public class OtlpMetricsConfig {

  @Bean
  public OtlpMeterRegistry otlpMeterRegistry() {
    Map<String, String> headers = parseKeyValues(System.getenv("OTEL_EXPORTER_OTLP_HEADERS"));
    Map<String, String> resourceAttributes = parseKeyValues(System.getenv("OTEL_RESOURCE_ATTRIBUTES"));
    resourceAttributes.putIfAbsent("service.name",
        System.getenv().getOrDefault("OTEL_SERVICE_NAME", "tacs-backend"));

    String url = construirUrlMetricas(System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT"));

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

      // Grafana Cloud (Mimir) es Prometheus por debajo: su modelo nativo es cumulative y
      // rechaza con HTTP 400 ("invalid temporality and type combination") varios tipos en delta
      // (counters de librerias, gauges del executor, timers, etc.). El agente OTel tambien
      // exporta en cumulative (ver Dockerfile), asi que todo el stack queda alineado.
      @Override
      public @NonNull AggregationTemporality aggregationTemporality() {
        return AggregationTemporality.CUMULATIVE;
      }
    };

    return new OtlpMeterRegistry(config, Clock.SYSTEM);
  }

  /**
   * Crea el registry solo si la env var del SO {@code OTEL_EXPORTER_OTLP_ENDPOINT} esta
   * presente. Misma fuente ({@code System.getenv}) que usa el agente OTel y que la lectura
   * de {@link #otlpMeterRegistry()}. Si no esta, no se define el bean y queda el
   * MeterRegistry de fallback de Spring Boot.
   */
  static class EndpointOtlpPresente implements Condition {
    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
      String endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
      return endpoint != null && !endpoint.isBlank();
    }
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
