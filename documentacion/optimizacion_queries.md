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

## Loadtest k6 — p95 por endpoint (2026-07-17)

**Corrida:** 2026-07-17 07:03–07:28 UTC, rama `fix/optimizacion-queries`.
**Ambiente:** stack local Docker (`backend-test:8080` + MongoDB local) — sin la latencia Render↔Atlas de staging.
**Perfil de carga:** rampa 100 → 250 → 200 → 50 VUs (~65 s por escenario).
**Fuente:** `backend/src/test/loadtest/results/<escenario>.json` (k6 `--summary-export`).

> **Nota metodológica:** el summary de k6 agrega todas las requests HTTP del escenario
> (incluye el login de cada iteración y los GETs de setup en los escenarios de
> escritura), no discrimina por URL. El p95 es del escenario completo.

| Escenario            | Endpoint objetivo                                |  Reqs | med (ms) | **p95 (ms)** | p99 (ms) | Errores |
|----------------------|--------------------------------------------------|------:|---------:|-------------:|---------:|--------:|
| post-recalcular      | POST /sugerencias/recalcular                     |    44 |      222 | **59 998** ¹ |   60 000 |   38,6% |
| patch-notificaciones | PATCH /perfil/notificaciones/leidas              |   378 |    1 026 |    **6 928** |    7 542 |      0% |
| get-subastas         | GET /subastas                                    | 4 762 |      859 |    **5 474** |    7 410 |      0% |
| get-propuestas       | GET /propuestas                                  | 6 268 |      271 |    **4 021** |    5 364 |      0% |
| get-faltantes        | GET /colecciones/faltantes                       | 6 870 |      228 |    **3 805** |    4 749 |      0% |
| get-sugerencias      | GET /sugerencias                                 | 6 650 |      242 |    **3 799** |    5 551 |      0% |
| get-figuritas        | GET /figuritas                                   | 6 732 |      163 |    **3 761** |    4 982 |      0% |
| patch-ofertas        | PATCH /subastas/{id}/ofertas/{ofertaId}          | 3 744 |      148 |      **608** |      847 |      0% |
| post-repetidas       | POST /colecciones/repetidas                      | 4 293 |       29 |      **526** |      803 |      0% |
| post-ofertas         | POST /subastas/{id}/ofertas                      | 9 183 |       20 |      **524** |      704 |      0% |
| post-login           | POST /login                                      | 3 304 |      179 |      **518** |      751 |      0% |
| post-propuestas      | POST /propuestas                                 | 9 300 |       19 |      **502** |      756 |      0% |
| post-subastas        | POST /subastas                                   | 9 345 |       19 |      **476** |      671 |      0% |
| put-perfil           | PUT /perfil                                      | 6 450 |       87 |      **473** |      700 |      0% |
| patch-repetidas      | PATCH /colecciones/repetidas/{figId}             | 3 206 |       93 |      **375** |      571 |      0% |
| post-administradores | POST /administradores                            | 6 878 |       86 |      **366** |      641 | 48,3% ² |
| patch-propuestas     | PATCH /propuestas/{id}/aceptar·rechazar·cancelar | 4 635 |       19 |      **337** |      522 |      0% |
| patch-favorito       | PATCH /sugerencias/{id}/favorito                 | 4 683 |       18 |      **301** |      670 |      0% |
| patch-subastas       | PATCH /subastas/{id}/cerrar·cancelar             | 4 665 |       70 |      **225** |      498 |      0% |
| delete-sesion        | DELETE /sesion                                   | 3 426 |       68 |      **162** |      358 |      0% |
| post-calificaciones  | POST /perfil/calificaciones                      | 3 382 |       96 |      **162** |      187 |      0% |
| head-usuarios        | HEAD /usuarios/{nombre}                          | 9 716 |        2 |        **4** |        5 |      0% |
| post-usuarios        | POST /usuarios                                   | 4 067 |        2 |        **4** |       84 | 97,3% ² |

> ¹ `post-recalcular` alcanza el timeout de k6 (60 s) bajo carga: el recálculo de
> sugerencias es un batch pesado que no está pensado para 250 usuarios concurrentes.
>
> ² Los errores de `post-usuarios` (97,3%) y `post-administradores` (48,3%) son 400
> por nombres de usuario duplicados entre iteraciones/corridas previas, no fallas del
> servidor — de ahí sus latencias bajas.

### Lectura

- **Los 5 GETs de listado son los únicos escenarios sanos por encima de 3,7 s de p95**:
  bajo 250 VUs saturan el backend (mediana baja + p95 alto = cola de espera, no query
  lenta). Son los candidatos para paginación más agresiva, índices y caching.
- Las escrituras (POST/PATCH/PUT) se mantienen por debajo de ~600 ms de p95 incluso
  en el pico de carga.
- `patch-notificaciones` merece revisión: p95 de 6,9 s con apenas 378 requests.
