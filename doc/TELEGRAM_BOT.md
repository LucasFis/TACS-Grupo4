# Bot de Telegram — Figuritas Mundial

## Índice
1. [Arquitectura general](#1-arquitectura-general)
2. [Inicialización y configuración](#2-inicialización-y-configuración)
3. [Estructura del paquete](#3-estructura-del-paquete)
4. [Sistema de handlers](#4-sistema-de-handlers)
   - [Interfaz BotHandler](#41-interfaz-bothandler)
   - [CommandHandler — enrutador central](#42-commandhandler--enrutador-central)
   - [Orden de procesamiento](#43-orden-de-procesamiento)
5. [Handlers disponibles](#5-handlers-disponibles)
   - [MenuHandler](#51-menuhandler)
   - [AuthHandler](#52-authhandler)
   - [ExplorarHandler](#53-explorarhandler)
   - [ColeccionHandler](#54-coleccionhandler)
   - [PropuestaHandler](#55-propuestahandler)
   - [SubastaHandler](#56-subastahandler)
6. [Flujos multi-paso](#6-flujos-multi-paso)
7. [Manejo de sesiones](#7-manejo-de-sesiones)
8. [Mensajes y paginación](#8-mensajes-y-paginación)
9. [Integración con el backend](#9-integración-con-el-backend)
10. [Notificaciones (AdapterTelegram)](#10-notificaciones-adaptertelegram)
11. [Tests](#11-tests)

---

## 1. Arquitectura general

```
Telegram User
     │
     ▼
Telegram Servers (Long Polling)
     │
     ▼
FiguritasBot.consume(List<Update>)
     │
     ▼
CommandHandler.handle(Update)
     │
     ├──► CallbackQuery? → match callbackPrefijos → handleCallback
     ├──► No text? → return null
     ├──► Pending flow? → handlePendiente (o cancel si es /command)
     ├──► Prefix match? → handle
     ├──► Exact match? → handle
     └──► Fallback → "Comando no reconocido"
     │
     ▼
BotResponse (texto + InlineKeyboardMarkup opcional)
     │
     ▼
FiguritasBot.enviar(chatId, BotResponse)
     │
     ▼
TelegramClient.execute(SendMessage) ──► Telegram User
```

El bot usa **long polling** (no webhooks). Corre dentro del mismo proceso Spring Boot del backend, por lo que los handlers tienen acceso directo a los servicios del negocio (`ServicioFigurita`, `ServicioColeccion`, etc.).

---

## 2. Inicialización y configuración

### Dependencias (pom.xml)

```xml
<dependency>
  <groupId>org.telegram</groupId>
  <artifactId>telegrambots-longpolling</artifactId>
  <version>7.10.0</version>
</dependency>
<dependency>
  <groupId>org.telegram</groupId>
  <artifactId>telegrambots-client</artifactId>
  <version>7.10.0</version>
</dependency>
```


```
### application.properties

```properties
telegram.bot.token=${TELEGRAM_BOT_TOKEN}
telegram.bot.username=${TELEGRAM_BOT_USERNAME}
```

### TelegramBotConfig.java

Clase `@Configuration` con perfil `!test` que:

1. Crea un bean `TelegramBotsLongPollingApplication`
2. Registra el bot via `CommandLineRunner` al iniciar la aplicación

```java
@Bean
public CommandLineRunner registerBot(TelegramBotsLongPollingApplication app,
                                     FiguritasBot figuritasBot) {
  return args -> { app.registerBot(botToken, figuritasBot); };
}
```

### FiguritasBot.java

`@Component` que implementa `LongPollingUpdateConsumer`. Es el punto de entrada de todos los updates de Telegram.

- Inyecta `CommandHandler` como cerebro del bot
- Crea un `OkHttpTelegramClient` con el token
- `consume(List<Update>)` — itera cada update, delega a `CommandHandler.handle()`, y envía la respuesta
- `enviar(chatId, BotResponse)` — construye un `SendMessage` con parse mode **Markdown** y teclado inline opcional
- `getChatId(Update)` — extrae el `chatId` tanto de mensajes regulares como de callbacks

---

## 3. Estructura del paquete

```
backend/src/main/java/app/telegram/
├── bot/
│   ├── TelegramBotConfig.java      # Configuración Spring
│   ├── FiguritasBot.java           # Punto de entrada del bot (LongPollingUpdateConsumer)
│   └── BotResponse.java            # Record: texto + InlineKeyboardMarkup opcional
├── handlers/
│   ├── BotHandler.java             # Interfaz que implementa cada handler
│   ├── CommandHandler.java         # Enrutador central de comandos/callbacks
│   ├── MenuHandler.java            # /start, /menu
│   ├── AuthHandler.java            # /login, /logout
│   ├── ExplorarHandler.java        # /explorar, /buscar
│   ├── ColeccionHandler.java       # /coleccion, /misfaltantes, /misrepetidas, /agfaltante, /agrepetida
│   ├── PropuestaHandler.java       # /propuestas, /enviadas, /recibidas, /proponer, /aceptar, /rechazar, /cancelar
│   └── SubastaHandler.java         # Gestión completa de subastas (~799 líneas)
├── sesion/
│   └── SessionManager.java         # Mapa en memoria chatId → JWT + pending field
└── utils/
    └── MessageBuilder.java         # Formateo de mensajes y teclados de paginación
```

---

## 4. Sistema de handlers

### 4.1 Interfaz BotHandler

Cada funcionalidad del bot se implementa en un handler que implementa esta interfaz:

```java
public interface BotHandler {
  Set<String> comandos();                              // Comandos exactos: "/menu"
  default Set<String> prefijos() { return Set.of(); }  // Comandos con args: "/buscar"
  BotResponse handle(Update update);

  // Flujo multi-paso
  default boolean tienePendiente(long chatId) { return false; }
  default BotResponse handlePendiente(Update update) { return null; }
  default void cancelarPendiente(long chatId) {}

  // Callbacks de botones inline
  default Set<String> callbackPrefijos() { return Set.of(); }
  default BotResponse handleCallback(Update update) { return null; }
}
```

### 4.2 CommandHandler — enrutador central

`@Component` que recibe todos los `BotHandler` registrados en Spring y construye tablas de ruteo en su constructor:

- `comandoAHandler: Map<String, BotHandler>` — comando exacto → handler
- `prefijosOrdenados: List<Entry<String, BotHandler>>` — prefijos ordenados de mayor a menor longitud
- `callbackAHandler: Map<String, BotHandler>` — prefijo de callback → handler

### 4.3 Orden de procesamiento

Para cada update, `CommandHandler.handle()` sigue este orden:

1. **CallbackQuery** → busca coincidencia en `callbackAHandler` y llama a `handleCallback()`
2. **Sin mensaje de texto** → retorna `null` (ignora)
3. **Flujo pendiente** → recorre todos los handlers buscando `tienePendiente(chatId)`. Si encuentra uno, y el mensaje **no** empieza con `/`, llama a `handlePendiente()`. Si empieza con `/`, cancela el flujo pendiente.
4. **Prefijo** → busca el prefijo más largo que coincida con el texto
5. **Comando exacto** → busca en `comandoAHandler`
6. **Fallback** → responde "❓ Comando no reconocido. Usá /menu para ver las opciones."

---

## 5. Handlers disponibles

### 5.1 MenuHandler

| Comandos | Callbacks | Auth | Descripción |
|----------|-----------|------|-------------|
| `/start`, `/menu` | — | No | Mensaje de bienvenida y menú principal |

`@Order(1)` — se carga primero junto con AuthHandler.

### 5.2 AuthHandler

| Comandos | Callbacks | Auth | Descripción |
|----------|-----------|------|-------------|
| `/login`, `/logout` | — | No (login) / Sí (logout) | Autenticación multi-paso |

**Flujo de login (multi-paso):**
1. Usuario escribe `/login`
2. Bot responde "Escribí tu nombre de usuario:"
3. Usuario escribe su username → se guarda en `pendingUsername[chatId]`
4. Bot responde "Ahora escribí tu contraseña:"
5. Usuario escribe su contraseña → se llama a `ServicioSesion.login(LoginRequest)`
6. Si OK → se guarda el JWT en `SessionManager.saveToken(chatId, token)`
7. Si error → "Credenciales inválidas"

**Restricciones:**
- Solo funciona en chat privado (no en grupos)
- Si ya está autenticado, informa y no permite re-login

### 5.3 ExplorarHandler

| Comandos | Prefijos | Callbacks | Auth |
|----------|----------|-----------|------|
| `/explorar` | `/buscar` | `figuritas:` | No |

- `/explorar` — muestra todas las figuritas intercambiables con paginación
- `/buscar <query>` — busca por nombre de jugador o selección
- Usa `MessageBuilder` para formatear cada figurita y el teclado de paginación
- Los botones "Anterior / Siguiente" usan callback data `figuritas:<nro_pagina>`
- Paginación de 5 elementos por página

### 5.4 ColeccionHandler

| Comandos | Callbacks | Auth |
|----------|-----------|------|
| `/coleccion`, `/misfaltantes`, `/misrepetidas`, `/agfaltante`, `/agrepetida` | `faltantes:`, `repetidas:` | Sí |

**Comandos:**
- `/coleccion` — menú de colección
- `/misfaltantes` — lista paginada de figuritas faltantes (obtiene `coleccionId` del JWT)
- `/misrepetidas` — lista paginada de repetidas, incluye cantidad disponible y métodos de intercambio
- `/agfaltante` — flujo multi-paso para agregar faltante (1 paso: ID de figurita)
- `/agrepetida` — flujo multi-paso de 3 pasos:
  1. ID de figurita
  2. Cantidad de unidades repetidas
  3. Método de intercambio (1 = intercambio, 2 = subasta, 3 = ambos)

**Datos temporales:** usa `estadoPendiente` y `datosPendientes` internos (además de `SessionManager`).

### 5.5 PropuestaHandler

| Comandos | Callbacks | Auth |
|----------|-----------|------|
| `/propuestas`, `/enviadas`, `/recibidas`, `/proponer`, `/aceptar`, `/rechazar`, `/cancelar` | `propuestas_enviadas:`, `propuestas_recibidas:` | Sí |

**Comandos:**
- `/propuestas` — menú de propuestas
- `/enviadas` — filtra por estado (1-4) y muestra propuestas enviadas paginadas
- `/recibidas` — filtra por estado y muestra propuestas recibidas paginadas
- `/proponer` — flujo multi-paso de 4 pasos:
  1. Nombre de usuario destinatario → busca en `RepositorioPerfiles.buscarPorNombre()`
  2. ID de figurita buscada (la que quiere el usuario)
  3. IDs de figuritas ofrecidas (separadas por coma)
  4. Confirmación (si/no)
- `/aceptar` — multi-paso: ingresa ID de propuesta → la acepta vía `ServicioPropuesta.aceptar()`
- `/rechazar` — multi-paso: ingresa ID de propuesta → la rechaza
- `/cancelar` — multi-paso: ingresa ID de propuesta → la cancela (solo para enviadas pendientes)

### 5.6 SubastaHandler

| Comandos | Prefijos | Callbacks | Auth |
|----------|----------|-----------|------|
| `/subasta`, `/subastas`, `/missubastas`, `/participadas` | `/crearsubasta`, `/ofertar`, `/editaroferta`, `/cancelaroferta`, `/seleccionar`, `/rechazaroferta`, `/cancelarsubasta`, `/cerrarsubasta` | `subastas_activas:`, `subastas_finalizadas:`, `subastas_participadas:` | Sí |

Es el handler más grande (~799 líneas). Maneja el ciclo de vida completo de subastas.

**Comandos principales:**
- `/subastas` — menú de subastas
- `/subasta` — multi-paso: ingresa ID → muestra detalle completo (incluye ofertas)
- `/missubastas` — elige activas (1) o finalizadas (2) y lista paginada
- `/participadas` — muestra subastas donde el usuario ofertó

**Comandos con argumentos (multi-paso con `prefijos`):**
- `/crearsubasta` — 4 pasos: figuritaID → duración (hs) → figuritas deseadas → calificación mínima
- `/ofertar` — 2 pasos: subastaID → figuritas ofrecidas
- `/editaroferta` — 3 pasos: subastaID → ofertaID → nuevas figuritas
- `/cancelaroferta` — 2 pasos: subastaID → ofertaID
- `/seleccionar` — 2 pasos: subastaID → ofertaID (elegir ganadora)
- `/rechazaroferta` — 2 pasos: subastaID → ofertaID
- `/cancelarsubasta` — 1 paso: subastaID
- `/cerrarsubasta` — 1 paso: subastaID

**Formateo de fechas:** usa `DateTimeFormatter` con locale `es_AR` para formato legible (ej: "25 jun 2026, 14:30").

---

## 6. Flujos multi-paso

Varios handlers implementan asistentes (wizards) de múltiples pasos. El mecanismo es:

1. El handler guarda el estado actual en `SessionManager.setPendingField(chatId, "estado")` o en un `Map<Long, String>` interno
2. `CommandHandler.handle()` detecta `tienePendiente(chatId)` y desvía a `handlePendiente()`
3. Cada paso valida la entrada y avanza al siguiente estado o ejecuta la acción final
4. Si el usuario escribe cualquier comando (`/...`), el flujo pendiente se cancela automáticamente

### Resumen de flujos

| Handler | Flujo | Pasos |
|---------|-------|-------|
| **AuthHandler** | Login | 1. username → 2. password → login |
| **ColeccionHandler** | Agregar faltante | 1. figId → agregar |
| **ColeccionHandler** | Agregar repetida | 1. figId → 2. cantidad → 3. método → agregar |
| **PropuestaHandler** | Crear propuesta | 1. destinatario → 2. fig buscada → 3. figs ofrecidas → 4. confirmar |
| **PropuestaHandler** | Aceptar/Rechazar/Cancelar | 1. ID propuesta → ejecutar |
| **PropuestaHandler** | Filtrar listas | 1. elegir filtro (1-4 o 0) → mostrar |
| **SubastaHandler** | Crear subasta | 1. figId → 2. duración → 3. figs deseadas → 4. calif. mínima → 5. confirmar |
| **SubastaHandler** | Ofertar | 1. subastaID → 2. figs ofrecidas |
| **SubastaHandler** | Editar oferta | 1. subastaID → 2. ofertaID → 3. nuevas figs |
| **SubastaHandler** | Cancelar oferta | 1. subastaID → 2. ofertaID |
| **SubastaHandler** | Seleccionar/Rechazar oferta | 1. subastaID → 2. ofertaID |
| **SubastaHandler** | Cancelar/Cerrar subasta | 1. subastaID |
| **SubastaHandler** | Mis subastas | 1. elegir activas(1) o finalizadas(2) → mostrar |

---

## 7. Manejo de sesiones

`SessionManager.java` — `@Component` que mantiene dos mapas en memoria (`ConcurrentHashMap`):

```java
private final Map<Long, String> sessions = new ConcurrentHashMap<>();      // chatId → JWT
private final Map<Long, String> pendingField = new ConcurrentHashMap<>();  // chatId → estado
```

**Métodos:**
- `saveToken(chatId, token)` / `getToken(chatId)` / `isAuthenticated(chatId)` / `logout(chatId)`
- `setPendingField(chatId, field)` / `getPendingField(chatId)` / `clearPendingField(chatId)`

**Nota:** Para un entorno productivo esto debería migrarse a Redis o base de datos. Para el alcance del TP, la memoria es suficiente.

Además, algunos handlers mantienen sus propios mapas de estado interno (como `ColeccionHandler.estadoPendiente` o `PropuestaHandler.datosPendientes`) para datos temporales más complejos que no caben en un solo string.

---

## 8. Mensajes y paginación

### BotResponse.java

Record inmutable que encapsula la respuesta del bot:

```java
public record BotResponse(String texto, InlineKeyboardMarkup teclado) { }
```

Métodos factory:
- `BotResponse.texto(String)` — solo texto, sin botones
- `BotResponse.conTeclado(String, InlineKeyboardMarkup)` — texto con teclado inline

### MessageBuilder.java

`@Component` con métodos de formateo:

- `formatearRepetida(FiguritaIntercambiableDto)` — formatea una figurita con emojis: número, jugador, selección, posición, métodos de intercambio, disponibilidad, usuario y reputación
- `formatearPagina(PaginaResultado)` — formatea una página completa con cabecera y lista de figuritas
- `tecladoPaginacion(paginaActual, totalPaginas, prefijo)` — crea botones "Anterior ⬅️" y "Siguiente ➡️" con callback data `<prefijo>:<nro_pagina>`

**Convención de callback data:** `<tipo>:<número_página>`
- `figuritas:0`, `figuritas:1`
- `faltantes:0`, `repetidas:0`
- `propuestas_enviadas:0`, `propuestas_recibidas:0`
- `subastas_activas:0`, `subastas_finalizadas:0`, `subastas_participadas:0`

---

## 9. Integración con el backend

Los handlers tienen acceso directo a los servicios Spring del backend a través de inyección de dependencias:

| Handler | Servicios utilizados |
|---------|---------------------|
| **AuthHandler** | `ServicioSesion.login(LoginRequest)` |
| **ExplorarHandler** | `ServicioFigurita.buscarFiguritas()`, `ServicioFigurita.buscarPorQuery()` |
| **ColeccionHandler** | `ServicioColeccion.buscarFaltantes()`, `buscarRepetidas()`, `agregarFaltante()`, `agregarRepetida()`, `ServicioJwt.getColeccionId()`, `getPerfilId()` |
| **PropuestaHandler** | `ServicioPropuesta.crearPropuesta()`, `buscarPropuestas()`, `aceptar()`, `rechazar()`, `cancelar()`, `RepositorioPerfiles.buscarPorNombre()` |
| **SubastaHandler** | `ServicioSubasta.obtenerSubasta()`, `obtenerSubastas()`, `crearSubasta()`, `ofertarEnSubasta()`, `editarOfertaEnSubasta()`, `cancelarOferta()`, `seleccionarOferta()`, `rechazarOferta()`, `cancelarSubasta()`, `cerrarSubasta()` |

Esto asegura que las operaciones realizadas desde Telegram sean consistentes con las realizadas desde la web.

---

## 10. Notificaciones (AdapterTelegram)

En `app.model.notificador` existe la clase `AdapterTelegram` que implementa `AdapterNotificacion`:

```java
public class AdapterTelegram implements AdapterNotificacion {
  public void notificar(Mensaje mensaje, Perfil receptor) {
    // vacío — pendiente de implementación
  }
}
```

Actualmente es un **stub** sin implementación. Está diseñado para integrarse con el sistema de notificaciones del backend y enviar mensajes proactivos a los usuarios de Telegram (ej: cuando reciben una propuesta, cuando ganan una subasta, etc.).

La entidad `MedioDeContacto` soporta el tipo `MedioComunicacion.TELEGRAM` que almacena el handle de Telegram del usuario (`@usuario`), vinculando la cuenta de Telegram con su perfil en la aplicación.

---

## 11. Tests

Los tests se encuentran en `backend/src/test/java/app/telegram/` y usan perfiles Spring para evitar la inicialización del bot real:

```properties
# test/resources/application.properties
telegram.bot.token=fake-token-para-test
telegram.bot.username=TestBot
```

| Test | Líneas | Lo que cubre |
|------|--------|-------------|
| `CommandHandlerTest.java` | 246 | Ruteo de comandos, callbacks, flujos pendientes, fallback |
| `MenuHandlerTest.java` | 78 | `/start`, `/menu` |
| `ColeccionHandlerTest.java` | 338 | Faltantes, repetidas, agregar faltante/repetida, paginación |
| `ExplorarHandlerTest.java` | 251 | Explorar, buscar, paginación |
| `SubastaHandlerTest.java` | 540 | Crear, ofertar, editar, cancelar, gestionar, listar |
| `PropuestaHandlerTest.java` | 444 | Crear propuesta, aceptar, rechazar, cancelar, filtrar |

---

## Referencias

- **Librería:** [TelegramBots v7.10.0](https://github.com/rubenlagus/TelegramBots) (long-polling)
- **Cliente HTTP:** OkHttp (integrado en `telegrambots-client`)
