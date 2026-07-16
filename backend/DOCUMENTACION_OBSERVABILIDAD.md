# Observabilidad del backend

## Stack

```
Spring Boot 3.2.5 (Java 17)
    │
    ├── OpenTelemetry Java Agent v2.28.1  → trazas + métricas de infra (JVM, HTTP)
    │       └── OTLP/HTTP → Grafana Cloud (Tempo para trazas, Mimir para métricas)
    │
    └── Micrometer OtlpMeterRegistry      → métricas custom (negocio + resiliencia)
            └── OTLP/HTTP → Grafana Cloud (Mimir)
```

**Por qué dos exportadores y no uno.**
El agente OTel instrumenta automáticamente JVM, HTTP y MongoDB, pero no puede ver los
`MeterRegistry` beans de Micrometer donde se registran los contadores de negocio.
`OtlpMeterRegistry` llena ese hueco reutilizando las mismas variables de entorno que
el agente (`OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_EXPORTER_OTLP_HEADERS`).

Para evitar duplicados se deshabilitan en Micrometer las familias que el agente ya cubre:

```properties
# application.properties
management.metrics.enable.jvm=false
management.metrics.enable.process=false
management.metrics.enable.system=false
management.metrics.enable.http=false
management.endpoints.web.exposure.include=
```

---

## Implementación en código

### `OtlpMetricsConfig.java`

Crea el `OtlpMeterRegistry` bean **solo si** `OTEL_EXPORTER_OTLP_ENDPOINT` está presente
en el entorno (via `@Conditional(EndpointOtlpPresente.class)`). Localmente y en tests la
variable no existe, así que Spring Boot usa su `SimpleMeterRegistry` de fallback y la app
arranca sin exportar nada.

Configuración relevante:

- **Temporalidad CUMULATIVE**: Grafana Cloud (Mimir) es Prometheus por debajo y rechaza
  métricas en modo DELTA. El agente OTel también exporta en cumulative, por lo que todo el
  stack queda alineado.
- **Histogramas exponenciales** (`base2_exponential_bucket_histogram`): mejor resolución de
  percentiles con muchas menos series que los buckets explícitos clásicos, lo que reduce la
  presión sobre el límite de cardinalidad del free tier.

### `AsyncConfig.java`

El pool de enriquecimiento de figuritas (1 hilo, `@Async`) se registra en Micrometer con:

```java
ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "agregar-datos-de-figuritas");
```

Esto expone automáticamente gauges del pool (`executor_pool_size`, `executor_queue_size`, etc.).

### `TheSportsDbImagenProveedor.java`

Registra métricas de Resilience4j en el constructor:

```java
TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry).

bindTo(meterRegistry);
TaggedRetryMetrics.

ofRetryRegistry(retryRegistry).

bindTo(meterRegistry);
```

Además mide la latencia de cada búsqueda con un `Timer.Sample`:

```java
Timer.Sample sample = Timer.start(meterRegistry);
// ... petición HTTP ...
sample.

stop(meterRegistry.timer("thesportsdb_busqueda_imagen_seconds", "resultado",resultado));
```

### Métricas de negocio en servicios

Cada servicio recibe el `MeterRegistry` por inyección de constructor (Lombok
`@RequiredArgsConstructor`) y llama a `.counter()`, `.timer()` o `.summary()` en los
puntos de interés.

---

## Catálogo de métricas

### Métricas de infraestructura — emitidas por el agente OTel

| Nombre                                 | Tipo       | Tags clave                                                                                      | Descripción                                                                  |
|----------------------------------------|------------|-------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| `http_server_request_duration_seconds` | Histograma | `http_route`, `http_request_method`, `http_response_status_code`, `deployment_environment_name` | Duración de cada request HTTP al backend. Base de p95/p99/error rate/uptime. |
| `jvm_memory_used_bytes`                | Gauge      | `jvm_memory_type`                                                                               | Heap y non-heap usado.                                                       |
| `jvm_memory_committed_bytes`           | Gauge      | `jvm_memory_type`                                                                               | Heap comprometido por la JVM.                                                |
| `jvm_gc_duration_seconds`              | Histograma | `jvm_gc_name`                                                                                   | Tiempo de cada ciclo de GC.                                                  |
| `jvm_cpu_recent_utilization_ratio`     | Gauge      | —                                                                                               | CPU del proceso (0–1).                                                       |
| `jvm_thread_count`                     | Gauge      | `jvm_thread_state`                                                                              | Threads de la JVM por estado.                                                |

### Métricas de MongoDB — derivadas de SpanMetrics (Tempo → Mimir)

Los paneles de MongoDB no usan métricas Micrometer sino SpanMetrics generadas por Grafana
Cloud a partir de las trazas de Tempo. Esto significa que MongoDB se observa de forma
transparente sin código adicional en el backend.

| Nombre                           | Tipo       | Tags clave                              | Descripción                                                         |
|----------------------------------|------------|-----------------------------------------|---------------------------------------------------------------------|
| `traces_spanmetrics_calls_total` | Counter    | `span_name`, `span_kind`, `status_code` | Operaciones MongoDB (find, aggregate, insert, update, delete, etc.) |
| `traces_spanmetrics_latency`     | Histograma | `span_name`                             | Latencia p95 por operación MongoDB.                                 |

### Métricas de negocio — emitidas por Micrometer

#### Counters

| Nombre                          | Tags                                                                    | Punto de emisión                         | Descripción                                            |
|---------------------------------|-------------------------------------------------------------------------|------------------------------------------|--------------------------------------------------------|
| `usuarios_registrados_total`    | `rol`                                                                   | `ServicioUsuario` tras guardar           | Registros nuevos, desglosados por rol (usuario/admin). |
| `auth_login_intentos_total`     | `resultado` (exitoso/fallido)                                           | `ServicioSesion`                         | Login exitosos vs fallidos.                            |
| `subastas_creadas_total`        | —                                                                       | `ServicioSubasta.crearSubasta`           | Subastas iniciadas.                                    |
| `propuestas_creadas_total`      | `metodo` (intercambio/subasta)                                          | `ServicioPropuesta`, `ServicioSubasta`   | Propuestas de intercambio creadas.                     |
| `propuestas_transiciones_total` | `estado` (aceptado/rechazado/cancelado), `origen` (intercambio/subasta) | `ServicioPropuesta`, `ServicioSubasta`   | Cambios de estado en propuestas.                       |
| `subastas_finalizadas_total`    | `resultado`                                                             | `ServicioSubasta.registrarCierreSubasta` | Subastas cerradas, por desenlace.                      |
| `calificaciones_creadas_total`  | `valor` (1–5)                                                           | `ServicioPerfil`                         | Calificaciones enviadas, desglosadas por puntaje.      |

#### Timers (histograma de duración)

| Nombre                                  | Tags                                                      | Punto de emisión                              | Descripción                                                     |
|-----------------------------------------|-----------------------------------------------------------|-----------------------------------------------|-----------------------------------------------------------------|
| `thesportsdb_busqueda_imagen_seconds`   | `resultado` (encontrada/no_encontrada/rate_limited/error) | `TheSportsDbImagenProveedor`                  | Latencia de cada llamada al API externo.                        |
| `propuestas_tiempo_resolucion_segundos` | `estado_final`                                            | `ServicioPropuesta.registrarTiempoResolucion` | Tiempo entre creación y resolución (aceptar/rechazar/cancelar). |
| `subastas_duracion_segundos`            | `resultado`                                               | `ServicioSubasta.registrarCierreSubasta`      | Duración real de la subasta (inicio → cierre).                  |

#### Distribution Summaries (histograma de valores)

| Nombre                              | Tags | Punto de emisión                         | Descripción                                                                                                          |
|-------------------------------------|------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| `figuritas_intercambiables_matches` | —    | `ServicioFigurita.mapearADto`            | Cuántos resultados devuelve cada búsqueda de figuritas intercambiables. Muchos ceros → inventario o matching pobres. |
| `subastas_ofertas_recibidas`        | —    | `ServicioSubasta.registrarCierreSubasta` | Cantidad de ofertas que tuvo cada subasta al cerrar.                                                                 |

#### Métricas de Resilience4j (auto-registradas)

| Nombre                                           | Tipo    | Descripción                                                                                                       |
|--------------------------------------------------|---------|-------------------------------------------------------------------------------------------------------------------|
| `resilience4j_ratelimiter_available_permissions` | Gauge   | Permisos disponibles en el rate limiter de TheSportsDB.                                                           |
| `resilience4j_ratelimiter_waiting_threads`       | Gauge   | Threads esperando un permiso.                                                                                     |
| `resilience4j_retry_calls_total`                 | Counter | Llamadas por desenlace: successful_without_retry, successful_with_retry, failed_with_retry, failed_without_retry. |

#### Métricas del executor @Async (auto-registradas)

| Nombre                | Tipo  | Descripción                                  |
|-----------------------|-------|----------------------------------------------|
| `executor_pool_size`  | Gauge | Hilos activos en el pool de enriquecimiento. |
| `executor_queue_size` | Gauge | Tareas encoladas esperando un hilo.          |

---

## Dashboard de Grafana

**UID:** `tacs-backend-observability`  
**URL:** `https://loyalcat2544.grafana.net/d/tacs-backend-observability`  
**Variable:** `$ambiente` (filtra por `deployment_environment_name`, p. ej. `staging` o `production`)  
**Paneles:** 37 en 5 secciones (rows)

### Sección 1 — Estado general

| Panel               | Tipo        | Qué muestra                                    |
|---------------------|-------------|------------------------------------------------|
| Requests por minuto | Time series | Throughput total del backend                   |
| Error rate 5xx      | Time series | % de respuestas 5xx sobre el total             |
| p95 HTTP            | Time series | Latencia p95 global                            |
| p99 HTTP            | Time series | Latencia p99 global                            |
| Uptime (sin 5xx)    | Stat        | % de requests sin 5xx en el rango seleccionado |

### Sección 2 — Endpoints

| Panel                     | Tipo        | Qué muestra                                                      |
|---------------------------|-------------|------------------------------------------------------------------|
| Requests por endpoint     | Time series | RPM por método+ruta                                              |
| p95 por endpoint          | Time series | Latencia p95 por endpoint — útil para detectar queries lentas    |
| Status codes por endpoint | Time series | RPM por método+ruta+código HTTP — muestra 4xx y 5xx por endpoint |

### Sección 3 — JVM

| Panel                      | Tipo        | Qué muestra                              |
|----------------------------|-------------|------------------------------------------|
| Heap usado vs comprometido | Time series | Memoria heap (usada y comprometida)      |
| Actividad de GC            | Time series | Tiempo de GC por colector (G1 Young/Old) |
| CPU del proceso            | Time series | CPU ratio 0–1 del proceso JVM            |
| Threads activos            | Time series | Conteo de threads por estado             |

### Sección 4 — Trazas y MongoDB

| Panel                                       | Tipo        | Qué muestra                                        |
|---------------------------------------------|-------------|----------------------------------------------------|
| Explore: trazas de tacs-backend             | Text/link   | Deeplink a Tempo para explorar trazas distribuidas |
| Operaciones MongoDB (por minuto)            | Time series | Volumen por operación (find, aggregate, update…)   |
| p95 latencia por operación MongoDB          | Time series | Latencia p95 por operación MongoDB via SpanMetrics |
| Errores en operaciones MongoDB (por minuto) | Time series | Operaciones con `status_code=ERROR`                |

### Sección 5 — Métricas de negocio y resiliencia

| Panel                                              | Tipo        | Qué muestra                                     |
|----------------------------------------------------|-------------|-------------------------------------------------|
| Login: intentos por resultado                      | Time series | Logins exitosos vs fallidos                     |
| Usuarios registrados por rol                       | Time series | Registros nuevos por rol                        |
| Subastas creadas                                   | Time series | Tasa de creación de subastas                    |
| Propuestas creadas por método                      | Time series | Intercambio directo vs subasta                  |
| Propuestas: transiciones por estado y origen       | Time series | Aceptadas/rechazadas/canceladas por canal       |
| Figuritas intercambiables: resultados por búsqueda | Time series | Promedio de matches por consulta                |
| TheSportsDB: enriquecimiento por resultado         | Time series | Imágenes encontradas, no encontradas, con error |
| TheSportsDB: latencia promedio                     | Time series | Latencia promedio del API externo               |
| TheSportsDB: rate limiter                          | Time series | Permisos disponibles y threads en espera        |
| TheSportsDB: reintentos por resultado              | Time series | Reintentos exitosos/fallidos                    |
| Calificaciones creadas por valor                   | Time series | Distribución de puntajes enviados               |
| Propuestas: tiempo de resolución promedio          | Time series | Días/horas hasta aceptar, rechazar o cancelar   |
| Subastas finalizadas por resultado                 | Time series | Tasa de cierre por desenlace                    |
| Subastas: duración promedio por resultado          | Time series | Tiempo de vida de las subastas                  |
| Subastas: ofertas recibidas                        | Time series | Promedio de ofertas por subasta al cerrar       |