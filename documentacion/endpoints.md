# Documentación de Endpoints — TACS-Grupo4

**Stack:** Java 17 + Spring Boot 3.2.5 · MongoDB
**Total:** 37 endpoints · 9 controladores

## Información general

| Aspecto | Detalle |
|---|---|
| Base URL | No especificada explícitamente (depende del deploy) |
| Formato request/response | JSON |
| Autenticación | JWT vía cookie `httpOnly` llamada `token` (expiración 12h) |
| Rutas públicas | `/login`, `/usuarios`, `/figuritas`, `/ping`, `/sesion`, `OPTIONS` |

---

## 1. ControladorPing

### `GET /ping`
- **Auth:** No
- **Descripción:** Verifica que el servidor esté activo.
- **Response `200 OK`:** `"pong"`

---

## 2. ControladorSesion

### `GET /administrador/estadisticas`
- **Auth:** Sí (rol `ADMIN` requerido en service)
- **Descripción:** Estadísticas generales del sistema.
- **Cookie:** `token` (JWT)
- **Query params:**
  - `desde` (String, requerido, formato `YYYY-MM-DD`)
  - `hasta` (String, requerido, formato `YYYY-MM-DD`)
- **Response `200 OK`:** `EstadisticasDto`

```json
{
  "totalUsuarios": "long",
  "totalFiguritasPublicadas": "long",
  "totalPropuestas": "int",
  "totalSubastasActivas": "int",
  "propuestasPorEstado": { },
  "figuritasPorModalidad": { },
  "rankings": { }
}
```

### `POST /login`
- **Auth:** No
- **Descripción:** Inicia sesión, setea cookie `token` httpOnly.
- **Body:**

```json
{
  "nombre": "string (not blank)",
  "contrasenia": "string (not blank)"
}
```

- **Response `204 No Content`** + `Set-Cookie: token=...`

### `GET /yo`
- **Auth:** Sí
- **Descripción:** Devuelve datos de sesión del usuario autenticado.
- **Cookie:** `token`
- **Response `200 OK`:**

```json
{
  "usuarioId": "string",
  "rol": "string",
  "perfilId": "string",
  "colId": "string"
}
```

### `DELETE /sesion`
- **Auth:** No (pero elimina cookie)
- **Descripción:** Cierra sesión expirando la cookie `token`.
- **Response:** `204 No Content`

---

## 3. ControladorUsuario

### `POST /usuarios`
- **Auth:** No
- **Descripción:** Registra un usuario estándar (rol implícito `USUARIO`).
- **Body:**

```json
{
  "nombre": "string (min 3, ^[a-zA-Z0-9_.]*$)",
  "contrasenia": "string (min 8, mayúsc + minúsc + número + caracter especial)"
}
```

- **Response:** `204 No Content`

### `PUT /usuarios/contrasenia`
- **Auth:** Sí
- **Descripción:** Cambia la contraseña del usuario autenticado.
- **Cookie:** `token`
- **Body:**

```json
{
  "contrasenia_actual": "string (not blank)",
  "contrasenia_nueva": "string (not blank)"
}
```

- **Response:** `200 OK`

### `POST /administradores`
- **Auth:** Sí (rol `ADMIN` requerido)
- **Descripción:** Registra un nuevo administrador.
- **Cookie:** `token`
- **Body:** mismo que `POST /usuarios`
- **Response:** `204 No Content`

### `HEAD /usuarios/{nombre}`
- **Auth:** No
- **Descripción:** Verifica disponibilidad de nombre de usuario.
- **Response:** `200 OK` si existe · `404 Not Found` si está disponible

---

## 4. ControladorFigurita

### `GET /figuritas/intercambiables`
- **Auth:** No
- **Descripción:** Figuritas intercambiables paginadas (búsqueda por texto libre o filtros).
- **Query params:**
  - `q` (String, opcional) — búsqueda de texto libre
  - `numero` (Integer, opcional)
  - `seleccion` (String, opcional)
  - `jugador` (String, opcional)
  - `tipo` (List\<MetodoIntercambio\>, opcional)
  - `pagina` (int, default: `0`)
  - `tamanioPagina` (int, default: `12`, máx: `40`)
- **Response `200 OK`:** `PaginaResultado<FiguritaIntercambiableDto>`

```json
{
  "contenido": [ ],
  "cantidadDeElementos": "long",
  "cantidadDePaginas": "int",
  "numero": "int"
}
```

### `GET /figuritas`
- **Auth:** No
- **Descripción:** Figuritas base (no intercambiables) con filtros.
- **Query params** (desde `FiguritasFiltro`):
  - `id` (String, opcional)
  - `numero` (Integer, opcional)
  - `seleccion` (String, opcional)
  - `jugador` (String, opcional)
  - `tipos` (List\<MetodoIntercambio\>, opcional)
  - `pagina` (Integer, opcional, def: `0`)
  - `tamanioPagina` (Integer, opcional, def: `40`, máx: `40`)
- **Response `200 OK`:** `List<FiguritaDto>`

---

## 5. ControladorColeccion

### `POST /colecciones/faltantes`
- **Auth:** Sí
- **Descripción:** Agrega figurita a faltantes de la colección.
- **Cookie:** `token`
- **Body:**

```json
{ "fig_id": "string (not blank)" }
```

- **Response:** `201 Created`

### `POST /colecciones/repetidas`
- **Auth:** Sí
- **Descripción:** Agrega figurita repetida (disponible para intercambio). Notifica a perfiles que la tienen como faltante.
- **Cookie:** `token`
- **Body:**

```json
{
  "fig_id": "string (not blank)",
  "cantidad_existente": "int (>0)",
  "modos_intercambio": ["ENVIO", "PUBLICAR_SUBASTA", "..."]
}
```

- **Enum `MetodoIntercambio`:** `ENVIO` · `PUBLICAR_SUBASTA` · `PUBLICAR_PROPUESTA`
- **Response:** `201 Created`

### `GET /colecciones/faltantes`
- **Auth:** Sí
- **Descripción:** Figuritas faltantes de la colección, paginadas.
- **Cookie:** `token`
- **Query params:** `limite` (int, opcional, def: `10`) · `pagina` (int, opcional, def: `1`)
- **Response `200 OK`:** `PaginaResultado<FiguritaDto>`

### `GET /colecciones/repetidas`
- **Auth:** Sí
- **Descripción:** Figuritas repetidas de la colección, con contadores de publicadas/disponibles.
- **Cookie:** `token`
- **Query params:**
  - `metodoIntercambio` (MetodoIntercambio, opcional)
  - `perfilId` (String, opcional — para cruzar con faltantes de otro perfil)
  - `limite` (int, opcional, def: `10`) · `pagina` (int, opcional, def: `1`)
- **Response `200 OK`:** `Repetidas<FiguritaIntercambiableDto>`

```json
{
  "publicadas": "int",
  "disponibles": "int",
  "contenido": [ ],
  "cantidadDeElementos": "long",
  "cantidadDePaginas": "int",
  "numero": "int"
}
```

### `PATCH /colecciones/repetidas/{fig_id}`
- **Auth:** Sí
- **Descripción:** Edita cantidad y modos de intercambio de una repetida.
- **Cookie:** `token`
- **Body:**

```json
{
  "cantidad_nueva": "int",
  "metodos": ["ENVIO", "..."]
}
```

- **Response:** `204 No Content`

---

## 6. ControladorPerfil

### `POST /perfil/calificaciones`
- **Auth:** Sí
- **Descripción:** Califica a otro perfil post-transacción.
- **Cookie:** `token`
- **Body:**

```json
{
  "valor": "int (1-5)",
  "destinatario_id": "string (not blank)",
  "descripcion": "string (opcional)",
  "transaction_id": "string (not blank)",
  "tipo_transaccion": "ENVIO | PUBLICAR_SUBASTA | PUBLICAR_PROPUESTA"
}
```

- **Response:** `200 OK`

### `GET /perfil/calificaciones`
- **Auth:** Sí
- **Descripción:** Calificaciones recibidas, paginadas.
- **Cookie:** `token`
- **Query params:** `pagina` (int, requerido) · `limite` (int, requerido)
- **Response `200 OK`:** `PaginaResultado<CalificacionDto>`

### `GET /perfil/contadores`
- **Auth:** Sí
- **Descripción:** Contadores de repetidas y faltantes del perfil.
- **Cookie:** `token`
- **Response `200 OK`:** `List<ContadorDto>` → `[{ "nombre": "...", "valor": "..." }]`

### `GET /perfil/notificaciones`
- **Auth:** Sí
- **Descripción:** Notificaciones del perfil, con paginación y filtro por estado de lectura.
- **Cookie:** `token`
- **Query params:**
  - `pagina` (opcional, default: 1) — número de página (1-based)
  - `limite` (opcional, default: 10) — cantidad de elementos por página
  - `leida` (opcional) — `true` para solo leídas, `false` para solo no leídas, omitir para todas
- **Response `200 OK`:** `PaginaResultado<NotificacionDto>` → `{ "contenido": [...], "cantidadDeElementos": 0, "cantidadDePaginas": 0, "numero": 1 }`

### `PATCH /perfil/notificaciones/leidas`
- **Auth:** Sí
- **Descripción:** Marca todas las notificaciones como leídas.
- **Cookie:** `token`
- **Response:** `204 No Content`

### `GET /perfil`
- **Auth:** Sí
- **Descripción:** Datos completos del perfil (incluye medios de contacto).
- **Cookie:** `token`
- **Response `200 OK`:** `PerfilDto`

```json
{
  "id": "string",
  "usuarioId": "string",
  "nombre": "string",
  "nombreUsuario": "string",
  "iniciales": "string",
  "calificacionMedia": "number",
  "mediosDeContacto": [
    { "medioComunicacion": "...", "valor": "..." }
  ]
}
```

### `PUT /perfil`
- **Auth:** Sí
- **Descripción:** Edita nombre, nombre de usuario y/o medios de contacto.
- **Cookie:** `token`
- **Body:**

```json
{
  "nombre": "string (not blank)",
  "nombre_usuario": "string (not blank)",
  "medios_de_contacto": [
    { "medio_comunicacion": "WHATSAPP|TELEGRAM|INSTAGRAM|OTRO", "valor": "string (not blank)" }
  ]
}
```

- **Response:** `200 OK`

---

## 7. ControladorPropuesta

### `POST /propuestas`
- **Auth:** Sí
- **Descripción:** Crea una propuesta de intercambio.
- **Cookie:** `token`
- **Body:**

```json
{
  "destinatario_id": "string (not blank)",
  "figurita_buscada_id": "string (not blank)",
  "figuritas_ofrecidas_ids": ["string"]
}
```

- **Response `201 Created`:** `PropuestaDto`

### `GET /propuestas/{prop_id}`
- **Auth:** Sí
- **Descripción:** Obtiene una propuesta por ID (clasificada como `ENVIADA` o `RECIBIDA` según el perfil).
- **Cookie:** `token`
- **Response `200 OK`:** `PropuestaDto`

### `GET /propuestas/{prop_id}/conflictos`
- **Auth:** Sí
- **Descripción:** Verifica si las figuritas a recibir están en otras transacciones pendientes.
- **Cookie:** `token`
- **Response `200 OK`:**

```json
{
  "tieneConflictos": "boolean",
  "figuritasEnConflicto": [ ]
}
```

### `PATCH /propuestas/{prop_id}/aceptar`
- **Auth:** Sí
- **Descripción:** Acepta la propuesta y ejecuta el intercambio.
- **Cookie:** `token`
- **Response:** `204 No Content`

### `PATCH /propuestas/{prop_id}/rechazar`
- **Auth:** Sí
- **Descripción:** Rechaza la propuesta, libera figuritas reservadas.
- **Cookie:** `token`
- **Response:** `204 No Content`

### `PATCH /propuestas/{prop_id}/cancelar`
- **Auth:** Sí (solo el autor)
- **Descripción:** Cancela la propuesta, libera figuritas reservadas.
- **Cookie:** `token`
- **Response:** `204 No Content`

### `GET /propuestas`
- **Auth:** Sí
- **Descripción:** Propuestas del perfil, paginadas y filtradas.
- **Cookie:** `token`
- **Query params:**
  - `tipo` (String, opcional)
  - `pagina` (int, opcional, def: `1`)
  - `limite` (int, opcional, def: `10`)
  - `estado` (EstadoProceso, opcional)
  - `idFiguritaBuscada` (String, opcional)
  - `idFiguritaPropuesta` (String, opcional)
- **Response `200 OK`:** `PaginaResultado<IntercambioDto>`

---

## 8. ControladorSubasta

### `POST /subastas`
- **Auth:** Sí
- **Descripción:** Crea una subasta para una figurita repetida.
- **Cookie:** `token`
- **Body:**

```json
{
  "figuritaId": "string (not blank)",
  "duracionEnHoras": "int (>0)",
  "figuritasDeseadasIds": ["string"],
  "calificacionMinima": "int (0-5, default: 0)"
}
```

- **Response:** `200 OK`

### `POST /subastas/{sub_id}/ofertas`
- **Auth:** Sí
- **Descripción:** Oferta en una subasta activa.
- **Cookie:** `token`
- **Body:**

```json
{ "figuritas_ofrecidas_id": ["string"] }
```

- **Response:** `200 OK`

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}`
- **Auth:** Sí
- **Descripción:** Modifica las figuritas ofrecidas en una oferta.
- **Cookie:** `token`
- **Body:**

```json
{ "figuritas_ofrecidas_id": ["string"] }
```

- **Response:** `200 OK`

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}/cancelar`
- **Auth:** Sí
- **Descripción:** Cancela una oferta, libera figuritas reservadas.
- **Cookie:** `token`
- **Response:** `200 OK`

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}/seleccionar`
- **Auth:** Sí (autor de subasta)
- **Descripción:** Selecciona una oferta como ganadora.
- **Cookie:** `token`
- **Response:** `200 OK`

### `PATCH /subastas/{sub_id}/ofertas/{oferta_id}/rechazar`
- **Auth:** Sí (autor de subasta)
- **Descripción:** Rechaza una oferta, libera figuritas reservadas.
- **Cookie:** `token`
- **Response:** `200 OK`

### `PATCH /subastas/{sub_id}/cancelar`
- **Auth:** Sí (autor de subasta)
- **Descripción:** Cancela la subasta completa, libera todas las figuritas.
- **Cookie:** `token`
- **Response:** `200 OK`

### `PATCH /subastas/{sub_id}/cerrar`
- **Auth:** Sí (autor de subasta)
- **Descripción:** Cierra la subasta; si hay oferta seleccionada, ejecuta el intercambio.
- **Cookie:** `token`
- **Response:** `200 OK`

### `GET /subastas`
- **Auth:** Sí
- **Descripción:** Subastas del perfil tipadas según contexto (participó, activas, finalizadas).
- **Cookie:** `token`
- **Query params:**
  - `pagina` (int, opcional, def: `1`)
  - `limite` (int, opcional, def: `10`)
  - `autorId` (String, opcional)
  - `participanteId` (String, opcional)
  - `estado` (String, opcional)
  - `idFiguritaSubastada` (String, opcional)
  - `idFiguritaOfertada` (String, opcional)
- **Response `200 OK`:** `PaginaResultado<?>` (el tipo varía: `MiSubastaActivaDto`, `MiSubastaFinalizadaDto`, `SubastaParticipoDto`)

### `GET /subastas/{sub_id}`
- **Auth:** No (no requiere cookie)
- **Descripción:** Obtiene una subasta por ID con todas sus ofertas.
- **Response `200 OK`:** `SubastaDto`

---

## 9. ControladorSugerencia

### `GET /sugerencias`
- **Auth:** Sí
- **Descripción:** Sugerencias de intercambio basadas en la colección del perfil.
- **Cookie:** `token`
- **Query params:** `pagina` (int, opcional, def: `1`) · `limite` (int, opcional, def: `10`)
- **Response `200 OK`:** `PaginaResultado<SugerenciaDto>`

### `PATCH /sugerencias/{sugerenciaId}/favorito`
- **Auth:** Sí
- **Descripción:** Alterna el estado de favorito de una sugerencia.
- **Cookie:** `token`
- **Response:** `204 No Content`

---

## Modelos de datos compartidos

### `PaginaResultado<T>` — respuesta paginada genérica

```json
{
  "contenido": [ "T" ],
  "cantidadDeElementos": "long",
  "cantidadDePaginas": "int",
  "numero": "int"
}
```

### `Repetidas<T>` — envoltorio de repetidas con contadores

```json
{
  "publicadas": "int",
  "disponibles": "int",
  "contenido": [ "T" ],
  "cantidadDeElementos": "long",
  "cantidadDePaginas": "int",
  "numero": "int"
}
```

### Enums

| Enum | Valores |
|---|---|
| `MetodoIntercambio` | `ENVIO` · `PUBLICAR_SUBASTA` · `PUBLICAR_PROPUESTA` |
| `MedioComunicacion` | `WHATSAPP` · `TELEGRAM` · `INSTAGRAM` · `OTRO` |

---

## Resumen de rutas públicas

| Verbo | Ruta |
|---|---|
| `GET` | `/ping` |
| `POST` | `/login` |
| `DELETE` | `/sesion` |
| `POST` | `/usuarios` |
| `HEAD` | `/usuarios/{nombre}` |
| `GET` | `/figuritas` |
| `GET` | `/figuritas/intercambiables` |
| `GET` | `/subastas/{sub_id}` |
| `OPTIONS` | *(preflight CORS)* |

---

**Total:** 37 endpoints · 9 controladores · Java 17 + Spring Boot 3.2.5 · MongoDB
