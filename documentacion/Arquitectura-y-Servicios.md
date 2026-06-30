# Arquitectura y Servicios — Backend

## Arquitectura en capas

El backend sigue una arquitectura en tres capas:

```text
Controller (endpoints REST)
      ↓
Service (lógica de negocio)
      ↓
Repository (persistencia MongoDB)
```

Los controladores son delgados: validan parámetros, delegan al servicio y devuelven la respuesta. Toda la lógica de negocio reside en los servicios.

---

## Servicios y sus responsabilidades

| Servicio | Responsabilidad |
|---|---|
| **ServicioUsuario** | Registro de usuarios/administradores, cambio de contraseña, verificación de disponibilidad de nombre. Usa BCrypt para encriptar contraseñas. |
| **ServicioSesion** | Autenticación de usuarios (login), validación de credenciales contra el repositorio, delegación de generación de JWT a ServicioJwt. |
| **ServicioJwt** | Creación, firma y validación de tokens JWT. Extrae claims (usuarioId, rol, perfilId, colId). Lee secret y expiración de application.properties. |
| **ServicioFigurita** | Búsqueda de figuritas base (catálogo) y figuritas intercambiables, ambas con filtros y paginación. |
| **ServicioColeccion** | Gestión de colecciones: agregar/editar/eliminar faltantes y repetidas, buscar con filtros y paginación, verificar conflictos. |
| **ServicioPerfil** | CRUD de perfil, calificaciones (agregar/obtener), notificaciones (obtener/marcar leídas), contadores de figuritas. Cachea la calificación media en el perfil para evitar cargar todas las calificaciones en memoria. |
| **ServicioPropuesta** | Ciclo de vida completo de propuestas de intercambio: crear, obtener, verificar conflictos, aceptar, rechazar, cancelar. Maneja reserva/liberación de figuritas. |
| **ServicioSubasta** | Ciclo de vida completo de subastas: crear, ofertar, editar/cancelar ofertas, seleccionar ganador, rechazar oferta, cerrar, cancelar subasta. |
| **ServicioSugerencia** | Obtención de sugerencias de intercambio generadas automáticamente, alternar estado favorito. |
| **ServicioNotificacion** | Envío y persistencia de notificaciones a perfiles (con/sin link). Se dispara al crear repetidas, recibir propuestas, etc. |
| **ServicioEstadisticas** | Estadísticas del sistema para el panel de administrador: usuarios, figuritas publicadas, propuestas, subastas activas, rankings. |
| **ServicioDeAgregacionDeDatos** | Implementación de ServicioEnriquecimiento. Procesa figuritas pendientes de imagen consultando TheSportsDB de forma asíncrona. |

---

## Configuraciones relevantes

### AsyncConfig
Configura un `ThreadPoolTaskExecutor` con 1 hilo y cola de 100 para el procesamiento asíncrono de enriquecimiento de imágenes. Monitoreado con Micrometer (`ExecutorServiceMetrics`).

### MongoConfig
Configura `MongoTransactionManager` para soportar transacciones en MongoDB (necesario para operaciones atómicas como reserva/confirmación de intercambios). Desactivado en perfiles `test` y `loadtest` porque Flapdoodle no soporta transacciones.

### OtlpMetricsConfig
Configura exportación OTLP de métricas custom de Micrometer (resilience4j, contadores de negocio) reutilizando variables de OpenTelemetry. Si no hay endpoint OTLP configurado, el bean no se crea (no impacta en entornos sin observabilidad).

### CorsConfig
Configura CORS global permitiendo el origen configurado en `cors.allowed-origin`, con credenciales y métodos HTTP estándar.

### Cronjobs
Ejecuta la generación de sugerencias de intercambio diariamente a las 3 AM y al iniciar la aplicación (solo si no es perfil `test`).

### InicializadorDeDatos
`CommandLineRunner` que carga datos semilla: 462 figuritas del Mundial Qatar 2022, usuarios demo, colecciones, subastas, propuestas. Existe una versión para perfil `test` con datos de prueba específicos.

---

## Patrones de diseño utilizados

### Adapter para notificaciones
`Notificador` usa una interfaz `AdapterNotificacion` que permite cambiar el canal de notificación. Actualmente existe `AdapterTelegram` pero **es un stub sin implementación**. La entidad `MedioDeContacto` soporta `MedioComunicacion.TELEGRAM` para vincular cuentas, pero el envío proactivo de mensajes desde el backend al usuario de Telegram no está implementado.

### Lazy loading manual
Para listas embebidas en MongoDB (como `Subasta.ofertas` o `Coleccion.repetidas`) no se puede usar el lazy nativo de Spring Data. Se crearon clases "proyección" que indican qué campos cargar en cada query, evitando traer listas pesadas cuando no se necesitan.

### PaginaResultado\<T\>
DTO genérico que unifica la respuesta de todas las listas paginadas:
```json
{
  "contenido": [ "T" ],
  "cantidadDeElementos": "long",
  "cantidadDePaginas": "int",
  "numero": "int"
}
```
Variantes como `Repetidas<T>` agregan contadores específicos (`publicadas`, `disponibles`).

### Proyecciones de repositorio
Se usan interfaces de proyección de Spring Data (ej: `FiguritaIntercambiableConPerfil`) para consultas optimizadas que no cargan documentos completos, solo los campos necesarios.

---

## Procesos de negocio principales

### Intercambio directo (propuesta)
1. Usuario A encuentra figurita de Usuario B que le falta
2. A crea propuesta → se reservan las figuritas ofrecidas
3. B recibe notificación y puede aceptar/rechazar
4. Si acepta → se ejecuta el intercambio: las figuritas intercambian de dueño, se liberan reservas
5. Si rechaza/cancela → se liberan las reservas
6. Ambos pueden calificarse mutuamente

### Subasta
1. Usuario crea subasta con figurita, duración, figuritas deseadas y calificación mínima
2. Otros usuarios ofertan con sus figuritas repetidas
3. El creador puede seleccionar una oferta ganadora, rechazar ofertas, o cancelar la subasta
4. Al cerrar con oferta seleccionada → se ejecuta el intercambio
5. Al cerrar sin selección o cancelar → se liberan todas las reservas

### Sugerencias automáticas
Un cronjob diario (y al iniciar la app) genera sugerencias de intercambio cruzando faltantes de un perfil con repetidas de otros perfiles. Se persisten en la colección `sugerencias` y los usuarios pueden marcarlas como favoritas.

### Calificaciones
Post-intercambio, los usuarios pueden calificarse del 1 al 5. Para evitar cargar todas las calificaciones en memoria cada vez que se calcula el promedio, se almacena `calificacionMedia` y `cantCalificaciones` en el perfil, actualizándolos incrementalmente.
