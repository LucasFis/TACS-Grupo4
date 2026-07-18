# Optimización de Queries — Comparativa de Response Times

## Contexto de medición

| Parámetro       | developer (baseline)                                          | fix/optimizacion-queries (actual)                         |
|-----------------|---------------------------------------------------------------|-----------------------------------------------------------|
| **Fecha**       | 2026-07-16T16:11:46Z                                          | 2026-07-17                                                |
| **Environment** | Staging — Render + MongoDB Atlas M0                           | Staging — Render + MongoDB Atlas M0                       |
| **Imagen**      | `valenaquino/tacs-backend:staging` (developer)                | `valenaquino/tacs-backend:staging` (fix, sha256:5a1430be) |
| **Usuario**     | `lucas_fis`                                                   | `lucas_fis`                                               |
| **Metodología** | Requests individuales vía curl, tiempo total cliente-servidor | Idem                                                      |

> **Nota:** Las mediciones son de request único (no p95 bajo carga). El ambiente Atlas M0 free tier
> introduce variabilidad de ±200ms entre mediciones. Los deltas en endpoints rápidos (<800ms) son
> predominantemente ruido de red/servidor.

---

## Cómo diagnosticamos los problemas

En vez de leer el código a ciegas, medimos con **trazas reales de staging**. El backend exporta trazas
distribuidas vía OpenTelemetry a Grafana Cloud (Tempo), con un span por cada operación MongoDB. Para
analizarlas usamos **Claude conectado al MCP de Grafana**: buscar las trazas más lentas por endpoint
(`service.name = tacs-backend`) y contar sus spans de Mongo convirtió el "está lento" en un número
accionable — **cuántos round-trips secuenciales a la base hacía cada endpoint**.

El diagnóstico fue el mismo en casi todos: un **N+1** (muchas queries triviales en cadena, no una
pesada). Como en staging cada operación Mongo cuesta ~169 ms fijos de red (Render Oregon ↔ Atlas M0), el
costo real era proporcional a la cantidad de idas y vueltas — un `/propuestas` de ~3,5 s eran ~19
operaciones encadenadas. La raíz eran los `@DBRef` de Spring Data, que resuelven cada referencia con un
find secuencial extra silencioso. Con las trazas señalando dónde estaba el N+1 de cada endpoint, la
remediación fue dirigida; los resultados se tabulan a continuación.

---

## Tabla comparativa

| Endpoint                                        | developer (ms) | fix (ms) | Δ% mejora   |
|-------------------------------------------------|----------------|----------|-------------|
| GET /ping                                       | 386            | 524      | −35.8% ¹    |
| GET /yo                                         | 415            | 452      | −8.9% ¹     |
| GET /figuritas                                  | 562            | 949      | −68.8% ¹    |
| GET /figuritas?seleccion=Argentina              | —              | 829      | —           |
| **GET /figuritas/intercambiables**              | **12 009**     | **938**  | **+92.2%**  |
| GET /figuritas/intercambiables?tipo=INTERCAMBIO | —              | 1 172    | —           |
| GET /figuritas/intercambiables?tipo=SUBASTA     | —              | 1 251    | —           |
| GET /subastas                                   | 27 253         | 852      | +96.9%      |
| GET /subastas?estado=ACTIVA                     | 20 401         | 640      | +96.9%      |
| GET /subastas?estado=FINALIZADA                 | 7 747          | 791      | +89.8%      |
| GET /subastas/{id}                              | 11 650         | 3 619    | +68.9%      |
| GET /propuestas?tipo=ENVIADAS                   | 26 660         | 775      | +97.1%      |
| GET /propuestas?tipo=RECIBIDAS                  | 14 436         | 796      | +94.5%      |
| GET /propuestas/{id}                            | 6 563          | 1 858    | +71.7%      |
| GET /sugerencias                                | 3 202          | 940      | +70.6%      |
| GET /colecciones/faltantes                      | 744            | 783      | −5.2% ¹     |
| GET /colecciones/repetidas                      | 2 642          | 929      | +64.8%      |
| GET /perfil                                     | 2 822          | 786      | +72.2%      |
| GET /perfil/{id}                                | 2 821          | 1 016    | +64.0%      |
| GET /perfil/notificaciones                      | 1 404          | 959      | +31.7%      |
| GET /perfil/calificaciones                      | 500 (error) ²  | 2 201    | ✅ corregido |
| GET /perfil/{id}/calificaciones                 | 500 (error) ²  | 3 029    | ✅ corregido |
| GET /perfil/contadores                          | 2 942          | 653      | +77.8%      |
| GET /perfil/{id}/contadores                     | 2 794          | 946      | +66.1%      |

> ¹ Regresión aparente en endpoints rápidos: dentro del margen de variabilidad de Atlas M0 (±200–400ms).
> No representan una regresión real — el tiempo absoluto sigue siendo sub-segundo.
>
> ² En developer, `/perfil/calificaciones` y `/perfil/{id}/calificaciones` retornaban HTTP 500 por
> un NPE en `CalificacionDto` (`c.getAutor()` nulo). El fix corrigió el null-check.

---

## Resumen de mejoras

### Endpoints más impactados

| Categoría                    | developer p50 aprox. | fix p50 aprox. | Reducción |
|------------------------------|----------------------|----------------|-----------|
| Propuestas (lista)           | ~20 000 ms           | ~780 ms        | **−97%**  |
| Subastas (lista)             | ~23 000 ms           | ~740 ms        | **−97%**  |
| Figuritas intercambiables    | ~12 000 ms           | ~938 ms        | **−92%**  |
| Perfil (contadores, detalle) | ~2 800 ms            | ~700 ms        | **−75%**  |
| Colecciones repetidas        | ~2 600 ms            | ~929 ms        | **−65%**  |

### Endpoints aún sobre 1 s en el fix

| Endpoint                                    | fix (ms) | Causa probable                                                  |
|---------------------------------------------|----------|-----------------------------------------------------------------|
| GET /subastas/{id}                          | 3 619    | Aggregation con lookup de ofertas (array de DBRefs)             |
| GET /perfil/{id}/calificaciones             | 3 029    | Aggregation sobre calificaciones con lookup de perfil           |
| GET /perfil/calificaciones                  | 2 201    | Idem                                                            |
| GET /propuestas/{id}                        | 1 858    | Lookup bidireccional (autor + destinatario + figuritas)         |
| GET /figuritas/intercambiables?tipo=SUBASTA | 1 251    | Pipeline con $lookup figuritas + $lookup perfiles + filtro tipo |

### Endpoints corregidos (error → OK)

- `GET /perfil/calificaciones`: 500 → 200 (fix de NPE en `CalificacionDto`)
- `GET /perfil/{id}/calificaciones`: 500 → 200 (mismo fix)

---

## Próximas optimizaciones identificadas

Los endpoints que permanecen sobre 1 s son candidatos para las siguientes mejoras,
detalladas en el análisis exhaustivo del pipeline de `/figuritas/intercambiables`:

1. **`$facet` count+results**: elimina la segunda pasada del pipeline de aggregation (~50% del tiempo de cualquier
   endpoint paginado).
2. **Embed campos de figurita**: mover `jugador`, `seleccion`, `numero` al documento embebido para evitar `$lookup` pre-
   `$limit`.
3. **Colección denormalizada**: única solución que hace `/figuritas/intercambiables` O(resultados) en vez de O(
   total_colecciones).

---

## Loadtest k6 — developer vs fix, p95 por endpoint (2026-07-17)

**Corrida fix:** 2026-07-17 07:03–07:28 UTC, rama `fix/optimizacion-queries`.
**Baseline developer:** 2026-07-17 08:04–08:29 UTC, rama `developer` — **misma máquina,
corridas consecutivas de la misma mañana**, por lo que la comparación de p95 es válida.
**Ambiente:** stack local Docker (`backend-test:8080` + MongoDB local) — sin la latencia Render↔Atlas de staging.
**Perfil de carga:** rampa 100 → 250 → 200 → 50 VUs (~65 s por escenario).
**Fuente:** `backend/src/test/loadtest/results/` — el baseline quedó preservado como
`<escenario>-developer-20260717.json` (los archivos `<escenario>.json` se sobrescriben
en cada corrida; los valores del fix son de la corrida de las 07:03 UTC).

> **Nota metodológica:** el summary de k6 agrega todas las requests HTTP del escenario
> (incluye el login de cada iteración y los GETs de setup en los escenarios de
> escritura), no discrimina por URL. El p95 es del escenario completo.

| Escenario              | Endpoint objetivo                                | p95 developer (ms) | p95 fix (ms) | **Δ% mejora p95** | med developer (ms) | med fix (ms) |
|------------------------|--------------------------------------------------|-------------------:|-------------:|------------------:|-------------------:|-------------:|
| post-recalcular ¹      | POST /sugerencias/recalcular                     |           59 998 ¹ |     59 998 ¹ |                0% |                219 |          222 |
| patch-notificaciones ¹ | PATCH /perfil/notificaciones/leidas              |           59 998 ¹ |        6 928 |        **+88,5%** |                205 |        1 026 |
| get-propuestas         | GET /propuestas                                  |             14 933 |        4 021 |        **+73,1%** |              2 915 |          271 |
| get-subastas           | GET /subastas                                    |             10 560 |        5 474 |        **+48,2%** |              2 015 |          859 |
| get-sugerencias        | GET /sugerencias                                 |              9 626 |        3 799 |        **+60,5%** |              1 607 |          242 |
| get-faltantes          | GET /colecciones/faltantes                       |              3 302 |        3 805 |          −15,2% ³ |                158 |          228 |
| get-figuritas          | GET /figuritas                                   |              3 136 |        3 761 |          −19,9% ³ |                142 |          163 |
| patch-propuestas       | PATCH /propuestas/{id}/aceptar·rechazar·cancelar |              2 729 |          337 |        **+87,7%** |                312 |           19 |
| patch-ofertas          | PATCH /subastas/{id}/ofertas/{ofertaId}          |              2 392 |          608 |        **+74,6%** |                372 |          148 |
| patch-subastas         | PATCH /subastas/{id}/cerrar·cancelar             |              1 441 |          225 |        **+84,4%** |                200 |           70 |
| patch-favorito         | PATCH /sugerencias/{id}/favorito                 |                950 |          301 |            +68,3% |                170 |           18 |
| put-perfil             | PUT /perfil                                      |                893 |          473 |            +47,0% |                146 |           87 |
| post-subastas          | POST /subastas                                   |                849 |          476 |            +43,9% |                 34 |           19 |
| post-repetidas         | POST /colecciones/repetidas                      |                655 |          526 |          +19,7% ³ |                101 |           29 |
| post-login             | POST /login                                      |                580 |          518 |          +10,7% ³ |                168 |          179 |
| post-propuestas        | POST /propuestas                                 |                515 |          502 |           +2,5% ³ |                 29 |           19 |
| post-ofertas           | POST /subastas/{id}/ofertas                      |                460 |          524 |          −13,9% ³ |                 26 |           20 |
| post-calificaciones    | POST /perfil/calificaciones                      |                228 |          162 |          +28,9% ³ |                121 |           96 |
| post-administradores ² | POST /administradores                            |                179 |          366 |         −104,5% ² |                 86 |           86 |
| patch-repetidas        | PATCH /colecciones/repetidas/{figId}             |                147 |          375 |         −155,1% ³ |                 63 |           93 |
| delete-sesion          | DELETE /sesion                                   |                139 |          162 |          −16,5% ³ |                 48 |           68 |
| head-usuarios          | HEAD /usuarios/{nombre}                          |                  3 |            4 |                 — |                  2 |            2 |
| post-usuarios ²        | POST /usuarios                                   |                  4 |            4 |               — ² |                  2 |            2 |

> ¹ Timeouts de k6 (60 s). `post-recalcular` colapsa en **ambas ramas** (batch pesado, no
> relacionado a este fix). `patch-notificaciones` colapsa **solo en developer** (44 reqs,
> 38,6% timeouts); en fix procesa 378 reqs sin errores — la mejora real es mayor que el +88,5%.
>
> ² Escenarios con errores 400 masivos por nombres de usuario duplicados entre corridas
> (`post-usuarios` ~97-99%, `post-administradores` ~48% en ambas ramas): sus latencias no
> son representativas y el delta no es significativo.
>
> ³ Deltas dentro del ruido entre corridas: escenarios sub-1s, o endpoints que esta rama
> no tocó (`GET /figuritas`, `GET /colecciones/faltantes` varían ±15-20% entre corridas
> idénticas).
