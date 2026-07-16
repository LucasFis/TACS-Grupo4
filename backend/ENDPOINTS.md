# Endpoints del backend

Base URL local: `http://localhost:8080`

## Autenticación

La mayoría de los endpoints requieren una cookie `token` (JWT httpOnly). Se obtiene con
`POST /login` y se elimina con `DELETE /sesion`.

**Rutas públicas** (sin cookie requerida): `/ping`, `/login`, `/sesion`, `/usuarios/**`,
`/figuritas`, `/figuritas/intercambiables`.

Los errores de validación devuelven `400` con body `{ "status": 400, "errors": { ... } }`.
Los errores de autenticación devuelven `401`. Los de autorización `403`. Los de recurso no
encontrado `404`.

---

## Ping

### `GET /ping`
Healthcheck del servidor.

**Auth:** no requerida  
**Response:** `200 OK` — body: `"pong"` (text/plain)

---

## Sesión

### `POST /login`
Inicia sesión. Establece la cookie `token` (httpOnly, Secure, SameSite=None, 12h TTL).

**Auth:** no requerida  
**Body:**
```json
{
  "nombre": "string",
  "contrasenia": "string"
}
```
**Response:** `204 No Content` + `Set-Cookie: token=<jwt>`  
**Errores:** `401` si las credenciales son incorrectas

---

### `GET /yo`
Devuelve los datos de sesión del usuario autenticado.

**Auth:** requerida  
**Response:** `200 OK`
```json
{
  "usuario_id": "string",
  "perfil_id": "string",
  "col_id": "string",
  "rol": "USUARIO | ADMIN"
}
```

---

### `DELETE /sesion`
Cierra sesión eliminando la cookie `token`.

**Auth:** no requerida  
**Response:** `204 No Content` + `Set-Cookie: token=; Max-Age=0`

---

## Usuarios

### `POST /usuarios`
Registra un nuevo usuario con colección y perfil vacíos.

**Auth:** no requerida  
**Body:**
```json
{
  "nombre": "string",       // mín. 3 chars, solo letras/números/_ .
  "contrasenia": "string",  // mín. 8 chars, requiere mayúscula, minúscula, número y carácter especial
  "rol": "USUARIO"
}
```
**Response:** `204 No Content`  
**Errores:** `400` si el nombre ya existe o la contraseña no cumple los requisitos

---

### `HEAD /usuarios/{nombre}`
Verifica si un nombre de usuario ya está en uso.

**Auth:** no requerida  
**Response:** `200 OK` si existe · `404 Not Found` si está disponible

---

### `PUT /usuarios/contrasenia`
Cambia la contraseña del usuario autenticado.

**Auth:** requerida  
**Body:**
```json
{
  "contrasenia_actual": "string",
  "contrasenia_nueva": "string"
}
```
**Response:** `200 OK`  
**Errores:** `401` si la contraseña actual es incorrecta

---

## Administradores

### `POST /administradores`
Registra un nuevo administrador. Solo puede ejecutarlo un admin existente.

**Auth:** requerida (rol `ADMIN`)  
**Body:** igual que `POST /usuarios`  
**Response:** `204 No Content`  
**Errores:** `403` si el usuario autenticado no es admin

---

### `GET /administrador/estadisticas`
Estadísticas generales del sistema en un rango de fechas.

**Auth:** requerida (rol `ADMIN`)  
**Query params:**

| Param | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `desde` | `YYYY-MM-DD` | sí | Fecha inicio del rango |
| `hasta` | `YYYY-MM-DD` | sí | Fecha fin del rango |

**Response:** `200 OK` — `EstadisticasDto` (usuarios, figuritas publicadas, propuestas, subastas, rankings, etc.)  
**Errores:** `400` si el formato de fecha es inválido o `desde > hasta` · `403` si no es admin

---

## Perfil

### `GET /perfil`
Devuelve los datos del perfil del usuario autenticado.

**Auth:** requerida  
**Response:** `200 OK` — `PerfilDto` (nombre, nombre_usuario, calificacion_promedio, medios_de_contacto, etc.)

---

### `PUT /perfil`
Edita el nombre, nombre de usuario y medios de contacto del perfil autenticado.

**Auth:** requerida  
**Body:**
```json
{
  "nombre": "string",
  "nombre_usuario": "string",
  "medios_de_contacto": [
    { "medio": "WHATSAPP | TELEGRAM | EMAIL", "valor": "string" }
  ]
}
```
**Response:** `200 OK`

---

### `POST /perfil/calificaciones`
Califica a otro perfil asociando la calificación a una transacción existente (intercambio o subasta).

**Auth:** requerida  
**Body:**
```json
{
  "destinatario_id": "string",
  "valor": 1,                         // 1–5
  "descripcion": "string",            // opcional
  "transaction_id": "string",
  "tipo_transaccion": "INTERCAMBIO | SUBASTA"
}
```
**Response:** `200 OK`  
**Errores:** `400` si ya existe calificación para esa transacción · `404` si el destinatario no existe

---

### `GET /perfil/calificaciones`
Devuelve las calificaciones recibidas por el perfil autenticado, paginadas.

**Auth:** requerida  
**Query params:**

| Param | Tipo | Requerido | Default |
|-------|------|-----------|---------|
| `pagina` | `int` | sí | — |
| `limite` | `int` | sí | — |

**Response:** `200 OK` — `PaginaResultado<CalificacionDto>`

---

### `GET /perfil/contadores`
Devuelve los contadores de repetidas y faltantes del perfil autenticado.

**Auth:** requerida  
**Response:** `200 OK` — `List<ContadorDto>` con campos `tipo` (repetidas/faltantes) y `cantidad`

---

### `GET /perfil/notificaciones`
Devuelve las notificaciones del perfil autenticado.

**Auth:** requerida  
**Response:** `200 OK` — `List<NotificacionDto>`

---

### `PATCH /perfil/notificaciones/leidas`
Marca todas las notificaciones del perfil autenticado como leídas.

**Auth:** requerida  
**Response:** `204 No Content`

---

## Colección

### `GET /colecciones/faltantes`
Devuelve las figuritas faltantes de la colección del usuario autenticado, paginadas.

**Auth:** requerida  
**Query params:**

| Param | Tipo | Default |
|-------|------|---------|
| `pagina` | `int` | `1` |
| `limite` | `int` | `10` |

**Response:** `200 OK` — `PaginaResultado<FiguritaDto>`

---

### `POST /colecciones/faltantes`
Agrega una figurita a los faltantes de la colección.

**Auth:** requerida  
**Body:**
```json
{ "fig_id": "string" }
```
**Response:** `201 Created`  
**Errores:** `400` si la figurita ya está en faltantes o no existe

---

### `GET /colecciones/repetidas`
Devuelve las figuritas repetidas de la colección del usuario autenticado, paginadas.

**Auth:** requerida  
**Query params:**

| Param | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| `pagina` | `int` | `1` | |
| `limite` | `int` | `10` | |
| `metodoIntercambio` | `INTERCAMBIO \| SUBASTA` | — | Filtra por modo de intercambio |
| `perfilId` | `string` | — | Filtra cruzando con los faltantes de ese perfil |

**Response:** `200 OK` — `Repetidas<FiguritaIntercambiableDto>` (incluye contadores de publicadas y disponibles)

---

### `POST /colecciones/repetidas`
Agrega una figurita repetida a la colección con cantidad y modos de intercambio.

**Auth:** requerida  
**Body:**
```json
{
  "fig_id": "string",
  "cantidad_existente": 3,                      // entero positivo
  "modos_intercambio": ["INTERCAMBIO", "SUBASTA"]
}
```
**Response:** `201 Created`  
**Errores:** `400` si la figurita ya está en repetidas o no existe

---

### `PATCH /colecciones/repetidas/{fig_id}`
Edita la cantidad disponible y los modos de intercambio de una repetida existente.

**Auth:** requerida  
**Path param:** `fig_id` — ID de la figurita  
**Body:**
```json
{
  "cantidad_nueva": 2,
  "metodos": ["INTERCAMBIO"]
}
```
**Response:** `204 No Content`  
**Errores:** `404` si la figurita no está en la colección

---

## Figuritas

### `GET /figuritas`
Lista todas las figuritas base del catálogo (no intercambiables). Útil para construir selectores.

**Auth:** no requerida  
**Query params:**

| Param | Tipo | Default |
|-------|------|---------|
| `id` | `string` | — |
| `numero` | `int` | — |
| `seleccion` | `string` | — |
| `jugador` | `string` | — |
| `pagina` | `int` | `0` |
| `tamanioPagina` | `int` | `40` (máx. 40) |

**Response:** `200 OK` — `List<FiguritaDto>`

---

### `GET /figuritas/intercambiables`
Lista las figuritas publicadas por otros usuarios como disponibles para intercambio, paginadas.

**Auth:** no requerida  
**Query params:**

| Param | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| `q` | `string` | — | Búsqueda por texto libre (si está presente, ignora los demás filtros) |
| `numero` | `int` | — | |
| `seleccion` | `string` | — | |
| `jugador` | `string` | — | |
| `tipo` | `INTERCAMBIO \| SUBASTA` | — | Puede enviarse múltiples veces |
| `pagina` | `int` | `0` | |
| `tamanioPagina` | `int` | `12` (máx. 40) | |

**Response:** `200 OK` — `PaginaResultado<FiguritaIntercambiableDto>`

---

## Propuestas (intercambio directo)

### `POST /propuestas`
Crea una propuesta de intercambio entre dos perfiles.

**Auth:** requerida  
**Body:**
```json
{
  "destinatario_id": "string",
  "figurita_buscada_id": "string",         // debe estar en los faltantes del autor
  "figuritas_ofrecidas_ids": ["string"]    // al menos una
}
```
**Response:** `201 Created` — `PropuestaDto`  
**Errores:** `400` si la figurita buscada no está en faltantes del autor · `404` si alguna figurita u ofertante no existe

---

### `GET /propuestas`
Lista las propuestas del usuario autenticado, paginadas.

**Auth:** requerida  
**Query params:**

| Param | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| `tipo` | `ENVIADAS \| RECIBIDAS` | — | |
| `estado` | `EstadoProceso` | — | `PENDIENTE`, `ACEPTADO`, `RECHAZADO`, `CANCELADO` |
| `idFiguritaBuscada` | `string` | — | |
| `idFiguritaPropuesta` | `string` | — | |
| `pagina` | `int` | `1` | |
| `limite` | `int` | `10` | |

**Response:** `200 OK` — `PaginaResultado<IntercambioDto>`

---

### `GET /propuestas/{prop_id}`
Devuelve los detalles de una propuesta.

**Auth:** requerida  
**Response:** `200 OK` — `PropuestaDto` (incluye campo `direccion`: `ENVIADA` o `RECIBIDA`)  
**Errores:** `404` si no existe

---

### `GET /propuestas/{prop_id}/conflictos`
Verifica si las figuritas ofrecidas en la propuesta están involucradas en otras transacciones del destinatario.

**Auth:** requerida  
**Response:** `200 OK`
```json
{
  "tiene_conflictos": true,
  "figuritas_en_conflicto": ["string"]
}
```

---

### `PATCH /propuestas/{prop_id}/aceptar`
Acepta una propuesta. Ejecuta el intercambio de figuritas entre los perfiles involucrados.

**Auth:** requerida (destinatario de la propuesta)  
**Response:** `204 No Content`  
**Errores:** `403` si el usuario no es el destinatario · `400` si la propuesta no está en estado `PENDIENTE`

---

### `PATCH /propuestas/{prop_id}/rechazar`
Rechaza una propuesta. Libera las figuritas reservadas del autor.

**Auth:** requerida (destinatario de la propuesta)  
**Response:** `204 No Content`

---

### `PATCH /propuestas/{prop_id}/cancelar`
Cancela una propuesta. Solo puede hacerlo el autor.

**Auth:** requerida (autor de la propuesta)  
**Response:** `204 No Content`

---

## Subastas

### `POST /subastas`
Crea una subasta para una figurita repetida del perfil autenticado.

**Auth:** requerida  
**Body:**
```json
{
  "figurita_id": "string",
  "duracion_en_horas": 24,              // entero positivo
  "figuritas_deseadas_ids": ["string"], // al menos una
  "calificacion_minima": 0             // 0–5, default 0
}
```
**Response:** `200 OK`  
**Errores:** `400` si la figurita no está en las repetidas del usuario

---

### `GET /subastas`
Lista las subastas del perfil autenticado. El contenido varía según los filtros:
- Con `participanteId`: subastas en las que el perfil participó como ofertante.
- Con `estado=ACTIVA`: subastas activas creadas por el perfil.
- Sin estado: subastas finalizadas creadas por el perfil.

**Auth:** requerida  
**Query params:**

| Param | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| `estado` | `string` | — | `ACTIVA`, `FINALIZADA`, etc. |
| `autorId` | `string` | — | |
| `participanteId` | `string` | — | |
| `idFiguritaSubastada` | `string` | — | |
| `idFiguritaOfertada` | `string` | — | |
| `pagina` | `int` | `1` | |
| `limite` | `int` | `10` | |

**Response:** `200 OK` — `PaginaResultado<MiSubastaActivaDto | MiSubastaFinalizadaDto | SubastaParticipoDto>`

---

### `GET /subastas/{sub_id}`
Devuelve los detalles de una subasta, incluyendo todas las ofertas activas.

**Auth:** no requerida  
**Response:** `200 OK` — `SubastaDto`  
**Errores:** `404` si no existe

---

### `POST /subastas/{sub_id}/ofertas`
Realiza una oferta en una subasta activa.

**Auth:** requerida  
**Body:**
```json
{ "figuritas_ofrecidas_id": ["string"] }
```
**Response:** `200 OK`  
**Errores:** `400` si la subasta no está activa · `403` si la calificación del ofertante es menor a la mínima

---

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}`
Modifica las figuritas ofrecidas en una oferta existente.

**Auth:** requerida (autor de la oferta)  
**Body:**
```json
{ "figuritas_ofrecidas_id": ["string"] }
```
**Response:** `200 OK`

---

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}/cancelar`
Cancela una oferta. Libera las figuritas reservadas.

**Auth:** requerida (autor de la oferta)  
**Response:** `200 OK`

---

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}/seleccionar`
Selecciona una oferta como ganadora. No cierra la subasta todavía.

**Auth:** requerida (autor de la subasta)  
**Response:** `200 OK`  
**Errores:** `403` si el usuario no es el autor de la subasta

---

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}/rechazar`
Rechaza una oferta. Libera las figuritas reservadas del ofertante.

**Auth:** requerida (autor de la subasta)  
**Response:** `200 OK`

---

### `PATCH /subastas/{sub_id}/cerrar`
Cierra la subasta. Si hay una oferta seleccionada, ejecuta el intercambio de figuritas.

**Auth:** requerida (autor de la subasta)  
**Response:** `200 OK`  
**Errores:** `400` si la subasta ya está cerrada

---

### `PATCH /subastas/{sub_id}/cancelar`
Cancela la subasta. Libera todas las figuritas reservadas (la subastada y las ofertadas).

**Auth:** requerida (autor de la subasta)  
**Response:** `200 OK`

---

## Sugerencias

### `GET /sugerencias`
Devuelve sugerencias de perfiles con los que el usuario autenticado podría intercambiar. El algoritmo cruza los faltantes del usuario con las repetidas disponibles (con modo `INTERCAMBIO`) de otros perfiles y viceversa.

**Auth:** requerida  
**Query params:**

| Param | Tipo | Default |
|-------|------|---------|
| `pagina` | `int` | `1` |
| `limite` | `int` | `10` |

**Response:** `200 OK` — `PaginaResultado<SugerenciaDto>`

---

### `PATCH /sugerencias/{sugerenciaId}/favorito`
Alterna el estado favorito de una sugerencia (true ↔ false). Las sugerencias no favoritas se eliminan periódicamente por un cronjob.

**Auth:** requerida (autor de la sugerencia)  
**Response:** `204 No Content`  
**Errores:** `404` si la sugerencia no existe o no pertenece al usuario
