# TACS 2026 1C — Grupo 5

Aplicación de intercambio de figuritas del Mundial. Permite gestionar colecciones, proponer intercambios, participar en subastas y calificarse entre usuarios.

## Stack tecnológico

### Backend

| Componente  | Tecnología        |
|-------------|-------------------|
| Lenguaje    | Java 17           |
| Framework   | Spring Boot 3.2.5 |
| Build       | Maven 3.9+        |
| Tests       | JUnit 5 + Mockito |
| Cobertura   | JaCoCo            |
| Calidad     | SpotBugs          |
| Boilerplate | Lombok            |

### Frontend

| Componente  | Tecnología         |
|-------------|--------------------|
| Lenguaje    | JavaScript         |
| Framework   | React 19           |
| Build       | Vite 8             |
| Router      | React Router 7     |
| HTTP Client | Axios              |
| Servidor    | Nginx (producción) |

### Infraestructura

| Componente       | Tecnología                 |
|------------------|----------------------------|
| Containerización | Docker (multi-stage build) |
| Orquestación     | Docker Compose             |
| CI               | GitHub Actions             |

## Requisitos

Solo necesitás Docker y Docker Compose. No requerís Java, Node.js ni MongoDB instalados localmente.

## Quick start

```bash
git clone <repo-url>
cd TACS-Grupo4
cp .env.example .env
make dev
```

La aplicación queda disponible en `http://localhost:5173`.

## Comandos

| Comando | Descripción |
|---------|-------------|
| `make up` | Build + levanta el stack en background |
| `make down` | Baja el stack |
| `make dev` | Levanta todo con hot reload (Mongo + backend + frontend) |
| `make dev-back` | Recarga solo el backend en dev |
| `make dev-front` | Recarga solo el frontend en dev |
| `make dev-down` | Baja el stack de desarrollo |
| `make logs` | Logs de todos los servicios |
| `make logs-back` | Logs solo del backend |
| `make logs-front` | Logs solo del frontend |
| `make test` | Corre tests del backend |

## URLs

| Servicio | URL |
|----------|-----|
| Frontend | `http://localhost:5173` |
| Backend  | `http://localhost:8080` |
| Mongo Express (dev) | `http://localhost:8081` |

## Variables de entorno

Copiar `.env.example` a `.env` y ajustar los valores según corresponda.

| Variable | Descripción | Valor por defecto / cómo obtenerlo |
|----------|-------------|-----------------------------------|
| `MONGO_URI` | URI de conexión a MongoDB | `mongodb://mongodb:27017/tacs` (dev). Para prod, usar Atlas |
| `CORS_ORIGIN` | Origen permitido para CORS | `http://localhost:5173` (dev). Para prod, el dominio real |
| `JWT_SECRET` | Secreto para firmar tokens JWT | Generar uno propio (ej: `openssl rand -base64 32`). No usar el placeholder del ejemplo |
| `JWT_EXPIRATION` | Tiempo de expiración del token | `12h` |
| `VITE_BACKEND_URI` | URL base del backend para el frontend | `http://localhost:8080` |
| `SEED_DATA` | Cargar datos de prueba al iniciar | `false` (dev). `true` si se quieren datos precargados |

Los valores por defecto funcionan para desarrollo (`make dev`). Para producción (`make up`), configurar `MONGO_URI` con una instancia de MongoDB externa (ej: Atlas) y `CORS_ORIGIN` con el dominio real.

## Postman

El archivo `postman_collection.json` en la raíz del repositorio contiene los endpoints preconfigurados.
