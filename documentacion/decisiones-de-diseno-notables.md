# Decisiones de diseño notables

---

## 1. JWT en cookie httpOnly (no localStorage)

**Problema:** Había que mantener sesión entre requests sin exponer el token al JavaScript del frontend.

**Decisión:** El JWT se guarda en una cookie httpOnly, secure, SameSite=Lax.

**Por qué:**
- El token no es accesible desde JS → mitigación contra XSS
- No hay que gestionar manualmente el envío del token en cada request (el browser lo hace automático)
- La cookie se elimina al cerrar sesión (simplemente expirándola)
- La expiración del token (12h) se maneja automáticamente: el interceptor de Axios detecta 401 y ejecuta logout

**Tradeoff:** Requiere `withCredentials: true` en Axios y configuración CORS específica. En iOS, al ser frontend y backend dominios diferentes en Render (same-origin issue con Public Suffix List), requirió un proxy Nginx (ver punto 2).

**Alternativa descartada:** localStorage + header Authorization. Se descartó por el riesgo XSS y porque requiere gestionar manualmente el token en cada request.

---

## 2. Proxy Nginx same-origin para iOS

**Problema:** Safari/WebKit en iOS bloquea cookies de terceros (ITP - Intelligent Tracking Prevention). Como frontend y backend están en dominios distintos de Render (`figunet.onrender.com` y `figunet-api.onrender.com`), la cookie de sesión era tratada como third-party y silenciosamente ignorada.

**Decisión:** Se implementó un proxy en Nginx dentro del contenedor del frontend. El browser solo habla con `figunet.onrender.com` y las requests a `/api/*` son redirigidas internamente al backend.

```
Browser → figunet.onrender.com/api/login
              ↓ Nginx (proxy interno)
          figunet-api.onrender.com/login
              ↓
          Set-Cookie: token=... (asociado a figunet.onrender.com)
```

**Cambios adicionales:**
- `SameSite=None` → `SameSite=Lax` (ahora es first-party, Lax es más seguro)
- `VITE_BACKEND_URI` se resuelve con `envsubst` al arrancar Nginx
- En desarrollo local, Vite tiene su propio proxy configurado en `vite.config.js`

**Impacto:** La app funciona correctamente en todas las plataformas (desktop, Android, iOS).

---

## 3. Telegram embebido en backend

**Problema:** Había que integrar un bot de Telegram para interactuar con el sistema.

**Decisión:** El bot corre dentro del mismo proceso Spring Boot, no como servicio aparte.

**Por qué (contexto de la decisión):**
- Tiempos acotados de desarrollo
- No agregar complejidad operativa (orquestar otro servicio, dockerizar, deployar)
- Los handlers del bot tienen acceso directo a los servicios del backend por inyección de dependencias → reutilizan toda la lógica existente

**Tradeoff:** El bot comparte recursos (CPU, memoria) con la API REST. Si el bot escala en uso, impacta en la latencia de la API.

**Mejora futura reconocida:** En producción con alta carga, el bot debería ser un servicio separado. También está pendiente la implementación de notificaciones proactivas (el `AdapterTelegram` es un stub vacío).

---

## 4. MongoDB con transacciones

**Problema:** Operaciones como aceptar una propuesta de intercambio involucran múltiples escrituras en distintas colecciones (reservar figuritas, crear propuesta, actualizar colecciones). Sin transacciones, una falla parcial podía dejar datos inconsistentes.

**Decisión:** Usar `MongoTransactionManager` de Spring Data MongoDB para operaciones que requieren atomicidad.

**Por qué:**
- MongoDB 4.0+ soporta transacciones multi-documento en replica sets
- Spring Data lo abstrae con `@Transactional`
- Las operaciones críticas (crear/aceptar propuestas, ofertar en subastas) quedan protegidas

**Consideraciones:**
- Flapdoodle (base embebida para tests) no soporta transacciones → se desactivan en perfil `test` y `loadtest`
- En desarrollo local se requiere replica set (configurado en `docker-compose.dev.yml` con `mongo-init`)

---

## 5. Optimización de calificaciones: promedio cacheado

**Problema:** Calcular el promedio de calificaciones cada vez que se muestra un perfil requería cargar todas las calificaciones en memoria, lo que no escala.

**Decisión:** Se almacenan `calificacionMedia` y `cantCalificaciones` en el documento `Perfil`. Al agregar una calificación nueva, se actualiza el promedio incrementalmente:

```
nuevaMedia = (mediaAnterior * cantAnterior + nuevaCalificacion) / (cantAnterior + 1)
```

**Tradeoff:** Se gana velocidad de lectura a cambio de espacio en disco (un campo extra en Perfil) y una mínima sobrecarga en escritura.

---

## 6. Catálogo de figuritas hardcodeado

**Decisión:** Las 462 figuritas del Mundial Qatar 2022 están hardcodeadas en la clase `FiguritasQatar2022` como un catálogo estático que se carga al iniciar la app.

**Por qué:**
- El set de figuritas es fijo y conocido (Qatar 2022)
- Evita depender de una API externa para el catálogo base
- Simplifica la inicialización de datos

**Mejora potencial:** Para un mundial en curso, lo ideal sería consumir una API oficial o mantenerlo en la base de datos con capacidad de actualización.

---

## 7. Perfiles de Spring para distintos entornos

El sistema define perfiles de Spring para manejar configuraciones diferenciales:

| Perfil | Uso |
|---|---|
| `default` / ningún perfil | Desarrollo local con datos semilla |
| `test` | Tests unitarios y de integración (Flapdoodle, sin bot de Telegram, sin transacciones) |
| `loadtest` | Tests de carga con K6 (similar a test pero sin inicialización de datos) |
| `!test` (perfil condicional) | Activa el bot de Telegram solo cuando no es test |

Esto permite que un solo código base se comporte distinto según el contexto sin modificar archivos.

---

## 8. Enriquecimiento de imágenes con Resilience4j

**Decisión:** El proveedor de imágenes (`TheSportsDbImagenProveedor`) usa Resilience4j con:
- **RateLimiter**: 1 request cada `60/rpm` segundos (default 1 req/2s). La API permite máximo 30 req/min.
- **Retry**: 3 reintentos con 60s de espera, solo ante HTTP 429.
- Las métricas se exportan vía Micrometer.

**Lock con findAndModify:** Para evitar que dos instancias procesen la misma figurita, se usa un lock optimista con `findAndModify` sobre la colección `figuritas`. Una figurita con `imagenStatus=EN_PROCESO` y `imagenCreadoEn` reciente no es reclamada por otra instancia.

---

## 9. CSS Modules + Bootstrap (no Material UI)

**Decisión:** Se usó Bootstrap como framework CSS en combinación con CSS Modules para estilos específicos.

**Por qué:**
- Bootstrap es liviano, conocido por el equipo y fácil de integrar
- CSS Modules da encapsulamiento real (los estilos de un componente no afectan a otros)
- No se necesitaba la complejidad ni el peso de Material UI
- La consigna recomendaba "algún framework CSS", no Material específicamente

---

## 10. Sin Redux ni librerías de estado externas

**Decisión:** Todo el estado global se maneja con Context API de React.

**Por qué:**
- La aplicación no tiene un árbol de estado lo suficientemente complejo como para justificar Redux
- 3 contextos (auth, error, toast) cubren todas las necesidades de estado global
- El estado de cada vista es local (hooks personalizados con useState)
- Menos dependencias, menos bundle size, menos complejidad

**Tradeoff:** Si la aplicación creciera significativamente, podría necesitar migrar a Zustand o Redux para evitar renders innecesarios por contextos muy poblados.
