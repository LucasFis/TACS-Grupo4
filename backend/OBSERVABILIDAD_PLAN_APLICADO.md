# Plan de observabilidad — qué se aplicó en este worktree

Worktree `worktree-observability-plan`, basado en `feature/observability`. Implementa el
subconjunto **seguro y verificable por revisión** del plan de remediación de
`ANALISIS_MEJORAS_OBSERVABILIDAD.md` (§13). **No se levantó la app ni se corrieron builds**
(hay otros agentes en worktrees y los contenedores/puertos colisionan); la validación fue
por revisión de código.

## Aplicado

| Plan | Cambio | Archivo |
|------|--------|---------|
| **R1** | Apagar `http.server.requests` de Micrometer (`management.metrics.enable.http=false`) para eliminar la triple exportación HTTP. El agente OTel queda como única fuente de HTTP. | `backend/src/main/resources/application.properties` |
| **R3** | Histogramas exponenciales + temporalidad delta del agente (menos series, mejores percentiles en el free tier). | `backend/Dockerfile` |
| **R4a** | `subastas_finalizadas_total{resultado=adjudicada\|sin_oferta\|cancelada}` en `cerrarSubasta`/`cancelarSubasta`. Cierra el "¿la subasta cumplió su propósito?". | `backend/src/main/java/app/servicios/ServicioSubasta.java` |
| **R4b** | Tag `origen=intercambio\|subasta` en **todos** los `propuestas_transiciones_total` (7 sitios) para no mezclar los dos funnels. | `ServicioPropuesta.java`, `ServicioSubasta.java` |
| **R5** | Guía de separación de ambientes (local/staging/prod) y nota de "vacío = no exporta". | `.env.example` |
| **R6** | Blueprint `render.yaml` con servicios `staging` (rama developer) y `production` (rama main), `envVarGroup` compartido y `deployment.environment.name` por ambiente. **Plantilla a validar antes de conectar.** | `render.yaml` (raíz) |

Notas de compatibilidad:
- R1/R3 no tocan código Java → sin impacto en tests.
- R4a/R4b usan el `MeterRegistry` que esos servicios **ya** tenían inyectado (PR #197) →
  no cambian constructores ni firmas → no rompen constructores de test existentes.
- `propuestas_transiciones_total` quedó con el **mismo conjunto de tags** (`estado`+`origen`)
  en los 7 sitios → series consistentes. Los dashboards que hoy agregan sin `origen` siguen
  funcionando (suman sobre el label).

## Iteración 2 — gaps resueltos con build disponible

Con Java 17 + build por contenedor efímero (`docker compose -p obsplan run --rm backend
mvn verify`, aislado para no colisionar con otros worktrees) se cerraron los gaps que
requerían tocar servicios y compilar. Commits locales, sin push.

| Gap | Cambio | Archivo |
|-----|--------|---------|
| **G6 / R4** | `thesportsdb_busqueda_imagen_seconds{resultado=encontrada\|no_encontrada\|rate_limited\|error}` (Timer). `MeterRegistry` ahora es campo; el constructor de test usa `SimpleMeterRegistry`. | `TheSportsDbImagenProveedor.java` |
| **G7 / R4** | `calificaciones_creadas_total{valor=1..5}`. `MeterRegistry` inyectado; `ServicioPerfilTest` actualizado. | `ServicioPerfil.java`, `ServicioPerfilTest.java` |
| **G8 / R4** | `figuritas_intercambiables_matches` (DistributionSummary) registrado en `mapearADto`. Inyección por contexto Spring (test `@Autowired`, sin tocar constructor). | `ServicioFigurita.java` |
| **G9 / R4** | `propuestas_tiempo_resolucion_segundos{estado_final}` (Timer, desde el primer PENDIENTE) y `subastas_duracion_segundos{resultado}` + `subastas_ofertas_recibidas` en el cierre. | `ServicioPropuesta.java`, `ServicioSubasta.java` |
| **G10** | Conteo de `propuestas_transiciones_total` de subastas centralizado en `contarTransicion(...)` (único lugar con el nombre+tags). Caminos manuales y por snapshot no se solapan → sin doble conteo. | `ServicioSubasta.java` |
| **G5 / G12** | `OtlpMeterRegistry` en temporalidad **delta** (alineado al agente) y construcción de URL tolerante a barra final. | `OtlpMetricsConfig.java` |
| **G11** | Se deshabilita la instrumentación del cliente HTTP saliente (`spring-web` + `http-url-connection`) para que la API key de TheSportsDB (en el path) **no** llegue a los spans. La visibilidad de TheSportsDB queda por la métrica G6. | `backend/Dockerfile` |

### Decisiones sobre gaps que NO eran cambios de código

- **G3 (executor).** Se **mantiene** `ExecutorServiceMetrics` de Micrometer: es la única
  fuente de `executor_queue_remaining_tasks`/`executor_queued_tasks` (las que alimentan la
  alerta de "cola llena = enriquecimientos descartados"). El agente OTel **no** emite esos
  gauges; su instrumentación de executors es para propagar contexto de trazas a `@Async` y
  **no** debe deshabilitarse. Conclusión: no hay duplicación real de esas series → sin cambio.
- **G4.** Se conserva el enfoque por propiedades (`management.metrics.enable.http=false`)
  en vez del `MeterFilter`: es suficiente para apagar `http.server.requests` de Micrometer y
  más simple. Queda por **confirmar en Grafana** que desaparecen `http_server_requests_*`.

## Pendiente — infraestructura / fuera del repo (no son cambios de código)

- **R2** — crear el servicio `staging` en Render (lo modela `render.yaml`, pero hay que
  conectarlo) y verificar que `deployment_environment_name` pase a tener `staging`.
- **R5 (externo)** — health check externo / Synthetic Monitoring para el cold start de
  Render (distinguir "app dormida" de "app caída").
- **R7** — SLOs y alertas con baseline real (técnicas y de negocio, burn-rate), provisionables
  vía el MCP de Grafana. Solo sobre `production`.
- **Fase 4** — logs a Loki correlacionados con `trace_id`; Collector solo si aparece alguno
  de los disparadores del §6 del análisis.

## Cómo validar cuando haya build disponible (sin colisionar con otros worktrees)

1. Compilar el worktree de forma aislada (idealmente fuera de docker compose compartido).
2. Desplegar primero a **staging** y confirmar en Grafana (MCP):
   - `http_server_requests_seconds_*` / `_milliseconds_*` **desaparecen**; queda solo
     `http_server_request_duration_seconds_*`.
   - aparece `subastas_finalizadas_total` con sus 3 valores de `resultado`.
   - `propuestas_transiciones_total` ahora tiene el label `origen`.
   - `deployment_environment_name` incluye `staging`.
3. Comparar percentiles p95/p99 staging vs production antes de promover R3.
</content>
