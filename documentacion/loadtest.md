# Load Tests - Evidencia y Resultados

## 1. Cuándo se corrieron

---
Tras una implementacion nueva, se busca realizar la prueba de carga para garantizar 
que los cambios no afecten negativamente al rendimiento para el usuario final. 
Esto nos permite que, por cada implementacion, se haga la prueba de que el sistema 
(bajo un contexto de mucha carga) no se vea afectado por los cambios. 


## 2. En qué ambiente

---

Las pruebas de carga se ejecutaron en un entorno separado de los demas, con un perfil de spring especializado para el mismo. 
Esto permite la funcionalidad selectiva del sistema en un entorno de pruebas (como no cargar las imagenes de jugadores en los test o 
buscar la conexion con telegram). En la siguiente tabla se muestra el contexto completo de ejecucion:

| Parámetro | Valor |
|-----------|-------|
| **Docker Compose** | `docker-compose.test.yml` |
| **Compose profile** | `test` |
| **Backend** | `backend-test:8080` (containerizado, target `dev`, Spring Boot via `mvn spring-boot:run`) |
| **MongoDB** | `mongodb-test:27017` (Mongo 7, Replica Set `rs0`, database `tacs-test`) |
| **Base URL del backend** | `http://backend-test:8080` |
| **Seed data** | `SEED_DATA=true` (dataset de prueba incluido en la imagen) |
| **Spring profile** | `loadtest` (definido en `docker-compose.test.yml`) |
| **k6 runner** | Imagen oficial `grafana/k6` (contenedor dedicado) |
| **Red** | Red interna de Docker Compose (sin exposición de puertos a host) |

### Cómo se ejecutó

```bash
# Correr todos los scripts
make loadtest

# Correr un script específico (ej: get/subastas)
make loadtest-script SCRIPT=get/subastas.js

# Correr todos los scripts de una carpeta (ej: post)
make loadtest-script SCRIPT=post
```

El `entrypoint.sh` detecta si recibe un nombre de carpeta o archivo y ejecuta en consecuencia con `k6 run`.

---

## 3. Qué dataset se utilizó

El dataset se genera automáticamente al iniciar el compose de test mediante `SEED_DATA=true`, empleando el archivo `InicializadorDeDatosTest`.
Incluyendo:

### Usuarios pre-cargados

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `Admin12!` | ADMINISTRADOR |
| `lucas_fis` | `Gordo123!` | USUARIO |
| `sofia_ape` | `Password1@` | USUARIO |
| `mati_crim` | `Wordpass1$` | USUARIO |
| `juan_jose` | `Una_contrasenia1` | USUARIO |
| `vale_gom` | `Passval2&` | USUARIO |
| `diego_ram` | `Diegopass2#` | USUARIO |

### Perfiles asociados (IDs hardcodeados en scripts)

| Perfil ID |
|-----------|
| `lucas-id-001` |
| `sofia-id-001` |
| `matias-id-001` |
| `juan-id-001` |
| `valentina-id-001` |
| `diego-id-001` |

### Recursos esperados en el dataset

- **Figuritas**: Catálogo completo de figuritas (mínimo 1 por usuario para que los test de colección funcionen).
- **Colecciones**: Cada usuario tiene figuritas faltantes y repetidas.
- **Subastas**: Al menos una subasta abierta por usuario con figuritas subastadas y deseadas.
- **Propuestas**: Al menos una propuesta por usuario (como autor y como destinatario).

> **IMPORTANTE:** Los scripts de escritura (`post/`, `patch/`, `delete/`) son tolerantes a 4xx (validación de negocio), no a 5xx. Si el dataset no tiene los recursos necesarios, los scripts simplemente no ejecutan la acción (retornan sin hacer request).

---

## 4. Perfiles de carga (options)

### Perfil LECTURA (GET / HEAD)
Usado por: `get/subastas`, `get/figuritas`, `get/propuestas`, `get/sugerencias`, `get/faltantes`, `head/usuarios`

| Stage | Duración | VUs |
|-------|----------|-----|
| Ramp-up | 10s | 0 → 100 |
| Sostenido alto | 15s | 100 → 250 |
| Pico | 20s | 250 → 200 |
| Ramp-down | 15s | 200 → 50 |
| Cooldown | 5s | 50 → 0 |

**Thresholds:**
- `http_req_failed` < 1% (tasa de error < 1%)
- `checks` > 95%

---

### Perfil ESCRITURA (POST / PUT)
Usado por: `post/login`, `post/usuarios`, `post/subastas`, `post/ofertas`, `post/propuestas`, `post/administradores`, `put/perfil`

| Stage | Duración | VUs |
|-------|----------|-----|
| Ramp-up | 10s | 0 → 50 |
| Sostenido | 15s | 50 → 100 |
| Pico | 20s | 100 → 80 |
| Ramp-down | 15s | 80 → 25 |
| Cooldown | 5s | 25 → 0 |

**Thresholds:**
- `http_req_failed` < 5% (tasa de error < 5%)
- `checks` > 95%

---

### Perfil ESCRITURA CON EFECTOS (PATCH / DELETE)
Usado por: `patch/ofertas`, `patch/subastas`, `patch/propuestas`, `patch/repetidas`, `patch/favorito`, `post/calificaciones`, `post/repetidas`, `delete/sesion`

| Stage | Duración | VUs |
|-------|----------|-----|
| Ramp-up | 10s | 0 → 20 |
| Sostenido | 15s | 20 → 50 |
| Pico | 20s | 50 → 40 |
| Ramp-down | 15s | 40 → 10 |
| Cooldown | 5s | 10 → 0 |

**Thresholds:**
- `http_req_failed` < 10% (tasa de error < 10%)
- `checks` > 90%

---

### Perfil PROBLEMÁTICO (endpoints con race conditions / CPU-intensive)
Usado por: `patch/notificaciones`, `post/recalcular`

| Stage | Duración | VUs |
|-------|----------|-----|
| Ramp-up | 10s | 0 → 10 |
| Sostenido | 15s | 10 → 25 |
| Pico | 20s | 25 → 20 |
| Ramp-down | 15s | 20 → 5 |
| Cooldown | 5s | 5 → 0 |

**Thresholds:**
- `http_req_failed` < 10%
- `checks` > 85%

---

## 5. Endpoints testeados y sus checks

### GET (5 scripts)

| Script | Endpoint | Checks |
|--------|----------|--------|
| `get/subastas.js` | `GET /subastas` | Status 200, body no vacío, paginado, tiene id, figurita_subastada, fecha_inicio, fecha_cierre |
| `get/figuritas.js` | `GET /figuritas` | Status 200, body no vacío, es array, no vacío, tiene id, numero, jugador |
| `get/propuestas.js` | `GET /propuestas` | Status 200, body no vacío, paginado, tiene id, autor, destinatario, figurita_buscada, figuritas_ofrecidas |
| `get/sugerencias.js` | `GET /sugerencias` | Status 200, body no vacío, paginado, tiene figuritas_recomendadas, figuritas_necesarias, sugerido, autor |
| `get/faltantes.js` | `GET /colecciones/faltantes` | Status 200, body no vacío, paginado, tiene id, numero, jugador |

### HEAD (1 script)

| Script | Endpoint | Checks |
|--------|----------|--------|
| `head/usuarios.js` | `HEAD /usuarios/{nombre}` | Status 200 o 404 |

### POST (8 scripts)

| Script | Endpoint | Checks |
|--------|----------|--------|
| `post/login.js` | `POST /login` | Status 204, Set-Cookie presente |
| `post/usuarios.js` | `POST /usuarios` | Status 204 |
| `post/administradores.js` | `POST /administradores` | Status 204 o 403 |
| `post/subastas.js` | `POST /subastas` | Status 201 o 4xx |
| `post/ofertas.js` | `POST /subastas/{id}/ofertas` | Status 201 o 4xx |
| `post/propuestas.js` | `POST /propuestas` | Status 201 o 4xx |
| `post/repetidas.js` | `POST /colecciones/repetidas` | Status 201 o 4xx |
| `post/calificaciones.js` | `POST /perfil/calificaciones` | Status 200-499 |
| `post/recalcular.js` | `POST /sugerencias/recalcular` | Status 204 |

### PATCH (6 scripts)

| Script | Endpoint | Checks |
|--------|----------|--------|
| `patch/ofertas.js` | `PATCH /subastas/{id}/ofertas/{id}` | Status 204 o 4xx |
| `patch/subastas.js` | `PATCH /subastas/{id}/cancelar` o `PATCH /subastas/{id}/cerrar` | Status 204 o 4xx |
| `patch/propuestas.js` | `PATCH /propuestas/{id}/cancelar` o `aceptar` o `rechazar` | Status 204 o 4xx |
| `patch/repetidas.js` | `PATCH /colecciones/repetidas/{id}` | Status 204 o 4xx |
| `patch/favorito.js` | `PATCH /sugerencias/{id}/favorito` | Status 204 o 4xx |
| `patch/notificaciones.js` | `PATCH /perfil/notificaciones/leidas` | Status 204 |

### PUT (1 script)

| Script | Endpoint | Checks |
|--------|----------|--------|
| `put/perfil.js` | `PUT /perfil` | Status 200 o 204 |

### DELETE (1 script)

| Script | Endpoint | Checks |
|--------|----------|--------|
| `delete/sesion.js` | `DELETE /sesion` | Status 204 |

---

## 6. Resultados de ejecución

### Formato esperado de la salida de k6

```
     data_received.............: ...
     data_sent.................: ...
     http_req_blocked..........: ...
     http_req_connecting.......: ...
     http_req_duration.........: avg=...ms  min=...ms  med=...ms  max=...ms  p(90)=...ms  p(95)=...ms  p(99)=...ms
     http_req_failed...........: ...% 
     http_reqs.................: ...
     iteration_duration........: ...
     iterations................: ...
     vus_max...................: ...
     vus.......................: ...
```

### Tabla resumen de resultados

| Método | Endpoint | VUs max | p95 (ms) | p99 (ms) | http_req_failed (%) | checks (%) | Throughput (req/s) |
|--------|----------|---------|----------|----------|---------------------|------------|-------------------|
| GET | `/subastas` | 250 | 1401.08 | N/A | 0.00 | 100.00 | 150.56 |
| GET | `/figuritas` | 250 | 159.62 | N/A | 0.00 | 100.00 | 265.26 |
| GET | `/propuestas` | 250 | 2150.83 | N/A | 0.00 | 100.00 | 125.73 |
| GET | `/sugerencias` | 250 | 415.51 | N/A | 0.00 | 97.00 | 240.82 |
| GET | `/colecciones/faltantes` | 250 | 233.43 | N/A | 0.00 | 100.00 | 260.65 |
| HEAD | `/usuarios/{nombre}` | 250 | 3.00 | N/A | 0.00 | 100.00 | 148.68 |
| POST | `/login` | 100 | 91.27 | N/A | 0.00 | 100.00 | 57.55 |
| POST | `/usuarios` | 100 | 102.74 | N/A | 0.00 | 100.00 | 56.93 |
| POST | `/administradores` | 100 | 116.08 | N/A | 0.00 | 100.00 | 104.63 |
| POST | `/subastas` | 100 | 111.41 | N/A | 0.00 | 100.00 | 166.02 |
| POST | `/subastas/{id}/ofertas` | 100 | 89.70 | N/A | 0.00 | 100.00 | 169.92 |
| POST | `/propuestas` | 100 | 86.93 | N/A | 0.00 | 100.00 | 170.50 |
| POST | `/colecciones/repetidas` | 50 | 104.82 | N/A | 0.00 | 100.00 | 80.18 |
| POST | `/perfil/calificaciones` | 50 | 86.75 | N/A | 0.00 | 100.00 | 54.26 |
| POST | `/sugerencias/recalcular` | 25 | 8993.89 | N/A | 0.00 | 100.00 | 3.47 |
| PATCH | `/subastas/{id}/ofertas/{id}` | 50 | 86.86 | N/A | 0.00 | 100.00 | 77.44 |
| PATCH | `/subastas/{id}/cancelar\|cerrar` | 50 | 89.48 | N/A | 0.00 | 100.00 | 77.65 |
| PATCH | `/propuestas/{id}/...` | 50 | 120.00 | N/A | 0.00 | 100.00 | 74.19 |
| PATCH | `/colecciones/repetidas/{id}` | 50 | 86.41 | N/A | 0.00 | 100.00 | 54.97 |
| PATCH | `/sugerencias/{id}/favorito` | 50 | 86.34 | N/A | 0.00 | 100.00 | 72.50 |
| PATCH | `/perfil/notificaciones/leidas` | 25 | 87.72 | N/A | 0.00 | 100.00 | 27.85 |
| PUT | `/perfil` | 100 | 117.26 | N/A | 0.00 | 100.00 | 109.28 |
| DELETE | `/sesion` | 50 | 89.79 | N/A | 0.00 | 100.00 | 55.26 |

> **Nota sobre p99:** k6 no exporta p99 en el `--summary-export` por defecto. Para obtenerlo, agregar `p(99)` a `options.summaryTrendStats` o configurar un threshold con `http_req_duration{p(99)}`. Todos los scripts mostraron 0% de `http_req_failed` a nivel HTTP.

---

## 7. Tasa de error

| Perfil | Threshold configurado | Resultado | Estado |
|--------|-----------------------|-----------|--------|
| Lectura (GET/HEAD) | `http_req_failed < 1%` | 0.00% | PASS |
| Escritura (POST/PUT) | `http_req_failed < 5%` | 0.00% | PASS |
| Escritura con efectos (PATCH/DELETE) | `http_req_failed < 10%` | 0.00% | PASS |
| Problemático (recalcular, notificaciones) | `http_req_failed < 10%` | 0.00% | PASS |

**Detalle:** Todos los endpoints devolvieron 0% de errores HTTP (status >= 500 o connection errors). Los scripts de escritura que aceptan 4xx como válido (por conflictos de negocio bajo carga concurrente) funcionaron correctamente: las respuestas 4xx se contabilizan como éxito en los checks (ej: `status 201 o 4xx`).

### Check failures notables

| Script | Check | Passes | Fails | Tasa de fallo |
|--------|-------|--------|-------|---------------|
| `get/sugerencias.js` | `tiene elementos` | 5287 | 2608 | 33.0% |

El check `tiene elementos` en `/sugerencias` falló en ~33% de las iteraciones. Esto **no es un error del servidor** sino que el dataset de seed no genera suficientes sugerencias para todos los usuarios bajo alta carga concurrente — algunos usuarios simplemente no tienen sugerencias pendientes.

---

## 8. Conclusiones

### Áreas que pasaron todos los thresholds

- **Todos los 23 scripts pasaron 0% de `http_req_failed`**, muy por debajo de los thresholds configurados (1%, 5%, 10%).
- **Endpoints de lectura (GET):** Manejan 250 VUs concurrentes con p95 razonable. `/figuritas` (159ms), `/colecciones/faltantes` (233ms) y `/sugerencias` (415ms) son rápidos. `/subastas` (1401ms) y `/propuestas` (2150ms) son los más pesados por la cantidad de datos que retornan (paginación con joins).
- **Endpoints de escritura (POST/PUT):** Todos bajo 120ms p95 con 100 VUs. Login (91ms), registro (102ms), crear subasta (111ms), ofertar (89ms), crear propuesta (86ms).
- **Endpoints de escritura con efectos (PATCH/DELETE):** Todos bajo 120ms p95 con 50 VUs. Cancelar/cerrar subasta (89ms), aceptar/rechazar propuesta (120ms), editar repetida (86ms), favorito (86ms), logout (89ms).
- **Notificaciones:** 87ms p95 con 25 VUs, dentro de lo esperado.

### Áreas que no pasaron / requieren optimización

- **`POST /sugerencias/recalcular`:** p95 de **8993ms** (~9 segundos) con solo 25 VUs. Es el endpoint más lento del sistema por considerable margen. Es CPU-intensive (recalcula sugerencias para todos los usuarios). Con 25 VUs generó solo 3.47 req/s. **Requiere optimización** si se espera que múltiples usuarios lo invoquen simultáneamente. Sino, se podria considerar la posibilidad de limitar las cantidad de peticiones por usuario (Ej: 1 recalculo por hora).
- **`GET /propuestas`:** p95 de **2150ms** con 250 VUs. El endpoint retorna datos pesados (autor, destinatario, figuritas ofrecidas). Bajo carga alta los tiempos se degradan significativamente.
- **`GET /subastas`:** p95 de **1401ms** con 250 VUs. Similar a propuestas, retorna estructuras complejas con paginación.

### Problemas detectados

1. **Recalcular sugerencias es un cuello de botella:** Con p95 de ~9s y throughput de apenas 3.47 req/s, este endpoint no escala. Bajo carga real con múltiples usuarios simultáneos, generaría timeouts o saturación de CPU.
2. **GETs pesados (subastas, propuestas):** Con p95 > 1s bajo 250 VUs, estos endpoints podrían causar problemas de UX si se consultan frecuentemente. La paginación ayuda pero el payload sigue siendo grande.
---

## Apéndice: Detalle de configuración de k6

### Helpers compartidos

| Archivo | Propósito |
|---------|-----------|
| `helpers/auth.js` | Función `login(base, usuario)` que retorna un token JWT o null |
| `helpers/checks.js` | `checkHttp(res, status)` y `checkPaginacion(elems)` |
| `helpers/options.js` | 4 perfiles de carga: `optionsLectura`, `optionsEscritura`, `optionsEscrituraConEfectos`, `optionsProblematico` |
| `helpers/usuarios.js` | Dataset de usuarios hardcodeados para autenticación |

### Notas sobre tolerancia a errores

Los scripts de escritura aceptan **4xx como éxito** (`status 201 o 4xx`). Esto es intencional: bajo carga concurrente es normal que haya conflictos de negocio (ej: dos VUs intentando ofertar en la misma subasta, o crear propuesta con figurita ya reservada). Lo que se mide es que **el servidor no devuelva 5xx** y que **responda dentro de los tiempos esperados**.
