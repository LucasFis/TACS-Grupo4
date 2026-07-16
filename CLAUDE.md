# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Qué es

App de intercambio de figuritas del Mundial (TACS 2026 1C - Grupo 5). Los usuarios gestionan colecciones, proponen
intercambios directos, participan en subastas y se califican entre sí. Monorepo con `backend/` (Spring Boot + MongoDB) y
`frontend/` (React + Vite), orquestados con Docker Compose.

## Comandos

Todo se maneja desde el `Makefile` en la raíz. Los comandos corren en contenedores; no requieren Java ni Node instalados
localmente.

| Comando                              | Descripción                                                             |
|--------------------------------------|-------------------------------------------------------------------------|
| `make dev`                           | Stack de desarrollo completo con Compose Watch (hot reload de frontend y backend) |
| `make dev-back`                      | Levanta **solo** el backend con watch (al cambiar Java, recompila incremental y reinicia solo) |
| `make dev-front`                     | Levanta/recarga solo el frontend con watch                              |
| `make dev-down`                      | Baja el stack de desarrollo                                             |
| `make test`                          | Corre los tests del backend en un contenedor efímero (`mvn test`)       |
| `make build`                         | Build de producción (corre `mvn verify`: tests + SpotBugs + JaCoCo)     |
| `make logs-back` / `make logs-front` | Logs en vivo de cada servicio                                           |

Servicios en dev: frontend `http://localhost:5173`, backend `http://localhost:8080`, mongo-express
`http://localhost:8081`.

Antes de levantar: `cp .env.example .env` (los defaults funcionan para dev).

### Correr un solo test del backend

```bash
docker compose -f docker-compose.dev.yml run --rm backend mvn test -Dtest=ServicioPropuestaTest
docker compose -f docker-compose.dev.yml run --rm backend mvn test -Dtest=ServicioPropuestaTest#nombreDelMetodo
```

### Validación completa (lo que corre en CI y en `make build`)

```bash
docker compose -f docker-compose.dev.yml run --rm backend mvn verify
```

`mvn verify` falla si la cobertura de líneas baja del **80%** por paquete (JaCoCo) o si SpotBugs encuentra issues. Los
paquetes de DTOs, `app.config`, `app` y `app.repositories` están excluidos de la cobertura (ver `pom.xml`).

### Lint del frontend

```bash
docker compose -f docker-compose.dev.yml run --rm frontend npm run lint
```

## Arquitectura del backend (`backend/src/main/java/app/`)

Spring Boot 3.2.5 / Java 17. Spanish-first: nombres de clases, paquetes y dominio en español.

Flujo de capas en cada request:

```
Controlador (controllers/)  →  Servicio (servicios/)  →  Repositorio (repositories/)  →  MongoDB
   REST, valida @Valid           lógica de negocio          interfaz + impl Mongo
   extrae perfilId del JWT
```

- **Controllers** son delgados: extraen el `perfilId` del token (cookie `token`) vía `ServicioJwt.getPerfilId(token)`,
  validan el request con `@Valid` y delegan al servicio. No contienen lógica de negocio.
- **Servicios** (interfaz `ServicioX` en `servicios/`) contienen la lógica. La mayoría tiene su lógica inline;
  `servicios/impl/` solo aloja implementaciones que lo ameritan (ej. `ServicioDeAgregacionDeDatos`).
- **Repositorios**: interfaz en `repositories/` + implementación `XMongo` en `repositories/impl/`. Las impl usan
  `MongoTemplate` directamente (no Spring Data repositories autogenerados), con `Aggregation` para queries complejas y
  `Update` parcial. Los objetos `Campos*` en `repositories/impl/campos/` indican qué campos persistir en updates
  selectivos (evita reescribir el documento entero). Proyecciones en `repositories/projections/`.

### Convenciones y puntos clave

- **JSON en `snake_case`**: configurado globalmente vía `spring.jackson.property-naming-strategy=SNAKE_CASE`. Los DTOs
  se escriben en camelCase Java y se serializan a snake_case.
- **Lombok** en todo el dominio (`@Getter`, `@Builder`, `@RequiredArgsConstructor`, etc.). Indentación de 2 espacios,
  fin de línea Unix.
- **Autenticación por JWT en cookie**: `JwtFilter` (`middlewares/`) valida el token en cada request salvo rutas
  públicas (`/login`, `/usuarios`, `/figuritas`, `/ping`, `/sesion`) y preflight `OPTIONS`. El token viaja en la cookie
  `token`, no en header `Authorization`. Roles en enum `Rol` (`/estadisticas` requiere admin).
- **Manejo de errores centralizado**: `ErrorHandler` (`@RestControllerAdvice`) mapea excepciones de dominio (
  `NotFoundException`, `BadRequestException`, `ForbiddenException`, `UnauthorizedException` en `exceptions/`) a
  `ErrorResponse` con el HTTP status correcto. Para devolver un error desde un servicio, lanzar la excepción de dominio
  correspondiente — no construir `ResponseEntity` de error a mano.
- **`MetodoIntercambio` es un enum** (`INTERCAMBIO`, `SUBASTA`), no una entidad — decisión deliberada (ver README "
  Decisiones de diseño").
- **`Coleccion` tiene repositorio propio** separado de `Perfil`: es un agregado con identidad y lógica propia (
  deduplicación de faltantes, acumulación de repetidas).
- **Cliente externo TheSportsDB** (`client/`): enriquece figuritas con imágenes de jugadores. Usa Resilience4j (
  `RateLimiter` + `Retry` ante HTTP 429). El enriquecimiento corre **asíncrono** en un pool de 1 hilo (`AsyncConfig`,
  `@Async`) para no bloquear requests.
- **Datos de prueba**: `InicializadorDeDatos` (`CommandLineRunner`, `@Profile("!test")`) precarga
  perfiles/figuritas/subastas al arrancar. Los IDs de la colección de Postman corresponden a estos datos.

### Tests del backend

JUnit 5 + Mockito. Los tests de integración (controllers, repositorios) extienden `MongoTestBase`, que usa **Mongo
embebido de Flapdoodle** con `@ActiveProfiles("test")` y limpia todas las colecciones en `@AfterEach`. Los tests de
servicios suelen mockear repositorios. Espejan la estructura de paquetes de `main`.

## Arquitectura del frontend (`frontend/src/`)

React 19 + Vite 8 + React Router 7, JavaScript (no TypeScript). Alias `@` → `src/` (configurado en `vite.config.js`).

- **`services/`**: capa de comunicación con el backend. Todo pasa por la instancia `api` de Axios en `services/api.js`,
  con `withCredentials: true` (envía la cookie JWT). `handleAxiosError` traduce errores HTTP a errores de dominio (
  `SERVER_DOWN`, `UNAUTHORIZED`, `FORBIDDEN`, `API_ERROR`, etc.). Un interceptor global dispara el evento `logout` ante
  un 401.
- **`contexts/`**: estado global vía React Context — `userContext` (`AuthProvider`, sesión), `errorContext`,
  `toastContext`. El árbol se envuelve en estos providers en `app-routes.jsx`.
- **Routing en 3 niveles** (`app-routes.jsx`): rutas `publicas`, `privadas` (envueltas en `RutaProtegida`, requieren
  sesión) y `privilegiadas` (envueltas en `RutaPrivilegiada`, requieren rol admin, ej. `/estadisticas`).
- **Componentes en capas**: `components/layouts/` (estructura de página), `components/ui/` (genéricos sin lógica de
  negocio), `views/public/` (páginas con lógica). Cada view compleja agrupa sus `hooks/`, `components/` y `tabs/`
  propios en su carpeta. Estilos con CSS Modules (`*.module.css`).
- **Variable de entorno**: `VITE_BACKEND_URI` (default `http://localhost:8080`).

## Infraestructura

Docker multi-stage (stages `dev` / `build`/`builder` / `runtime`/`production`). El stack dev usa MongoDB como **replica
set** (`rs0`) — `mongo-init` lo inicializa idempotentemente. El backend expone `/ping` como healthcheck; en prod el
frontend espera a que el backend esté `healthy` antes de arrancar. CORS configurable vía `CORS_ORIGIN` sin recompilar.
