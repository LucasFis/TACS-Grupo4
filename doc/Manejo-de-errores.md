# Manejo centralizado de errores

Este documento describe la arquitectura y el flujo de manejo de errores implementado entre frontend y backend, con el objetivo de:

- Centralizar el tratamiento de errores.
- Evitar lógica repetida en componentes.
- Unificar el formato de respuestas.
- Redirigir automáticamente ante errores críticos.
- Permitir mostrar mensajes de validación y negocio dentro de la UI.

---

# Arquitectura general

El flujo sigue esta secuencia:

```text
Backend lanza excepción
        ↓
ErrorHandler captura la excepción
        ↓
Devuelve ErrorResponse uniforme
        ↓
Axios recibe la respuesta
        ↓
handleAxiosError transforma el error HTTP
        ↓
ErrorContext decide cómo actuar
        ↓
UI muestra mensajes o redirige
```

---

# Backend

## ErrorHandler (`@RestControllerAdvice`)

El backend utiliza un manejador global de excepciones:

```java
@RestControllerAdvice
public class ErrorHandler
```

Su responsabilidad es interceptar excepciones lanzadas desde cualquier controlador o servicio y devolver una respuesta consistente.

---

## Estructura de respuesta

Todas las excepciones generan un objeto `ErrorResponse`:

```json
{
    "code": 404,
    "message": "La propuesta no existe",
    "errors": {},
    "timestamp": "2026-05-20T20:15:00"
}
```

Campos:

| Campo | Descripción |
|---------|-------------|
| `code` | Código HTTP |
| `message` | Mensaje descriptivo |
| `errors` | Errores específicos por campo |
| `timestamp` | Momento del error |

---

## Manejo de excepciones específicas

### NotFoundException

```java
@ExceptionHandler(NotFoundException.class)
```

Devuelve:

```http
404 NOT FOUND
```

Ejemplo:

```json
{
    "code":404,
    "message":"La propuesta no existe"
}
```

Se utiliza cuando un recurso solicitado no fue encontrado.

---

### BadRequestException

```java
@ExceptionHandler(BadRequestException.class)
```

Devuelve:

```http
400 BAD REQUEST
```

Ejemplo:

```json
{
    "code":400,
    "message":"El usuario ya existe"
}
```

Se utiliza para errores de validación o reglas de negocio.

---

### UnauthorizedException

```java
@ExceptionHandler(UnauthorizedException.class)
```

Devuelve:

```http
401 UNAUTHORIZED
```

Ejemplo:

```json
{
    "code":401,
    "message":"Sesión inválida"
}
```

Se utiliza cuando el usuario no posee una sesión válida.

---

### Excepciones no controladas

```java
@ExceptionHandler(Exception.class)
```

Devuelve:

```http
500 INTERNAL SERVER ERROR
```

Ejemplo:

```json
{
    "code":500,
    "message":"Ocurrió un error interno del servidor"
}
```

Esto evita exponer detalles internos del sistema.

---

# Frontend

El frontend divide el manejo de errores en dos capas:

1. **Axios → Traducción de errores HTTP**
2. **ErrorContext → Decisión sobre qué hacer con el error**

---

# Axios

Archivo principal:

```js
const api = axios.create(...)
```

Configuración:

```js
const api = axios.create({
    baseURL: "...",
    timeout: 10000,
    headers:{
        "Content-Type":"application/json"
    },
    withCredentials:true
})
```

Parámetros importantes:

| Configuración | Función |
|---------|-------------|
| `baseURL` | URL base del backend |
| `timeout` | Tiempo máximo de espera |
| `withCredentials` | Envía cookies automáticamente |

---

# Transformación de errores

La función:

```js
handleAxiosError(error)
```

convierte errores propios de Axios a errores del dominio de la aplicación.

---

## Servidor caído

Si Axios detecta un error de red:

```js
if(error.code==="ERR_NETWORK")
```

Se transforma en:

```js
{
    type:"SERVER_DOWN",
    message:"Servidor no disponible"
}
```

---

## Errores HTTP

Según el código HTTP:

### 401

```js
{
    type:"UNAUTHORIZED",
    message:"No se encontro una sesión"
}
```

---

### 403

```js
{
    type:"FORBIDDEN",
    message:"Accion no permitida"
}
```

---

### 500

```js
{
    type:"INTERNAL_SERVER_ERROR",
    message:"Ocurrió un error interno"
}
```

---

### Otros errores

Para errores que deben mostrarse en pantalla:

```js
{
    type:"API_ERROR",
    status,
    message,
    errors
}
```

Ejemplo:

```js
{
    type:"API_ERROR",
    status:400,
    message:"El email ya existe",
    errors:{}
}
```

---

# Interceptor de Axios

Existe un interceptor global:

```js
api.interceptors.response.use(...)
```

Su objetivo es detectar sesiones inválidas automáticamente.

Cuando recibe:

```http
401 Unauthorized
```

ejecuta:

```js
await logout()
```

y luego:

```js
window.dispatchEvent(
    new Event("logout")
)
```

Esto permite que cualquier componente que escuche ese evento actualice el estado global del usuario.

Flujo:

```text
JWT expira
      ↓
Backend devuelve 401
      ↓
Interceptor ejecuta logout
      ↓
Se dispara evento "logout"
      ↓
Contexto de usuario elimina usuario actual
      ↓
La UI se actualiza automáticamente
```

---

# ErrorContext

El `ErrorContext` centraliza la lógica de presentación de errores.

```js
const ErrorContext = createContext()
```

Su función principal es:

```js
handleError(error,setter)
```

---

## Errores que generan navegación automática

### SERVER_DOWN

Redirige a:

```text
/servidor-caido
```

Además guarda la ruta actual:

```js
navigate("/servidor-caido",{
    state:{
        from:location.pathname
    }
})
```

Esto permite luego volver a la página anterior si el servidor vuelve a estar disponible.

---

### FORBIDDEN

Redirige a:

```text
/unauthorized
```

---

### INTERNAL_SERVER_ERROR

Redirige a:

```text
/error-interno
```

---

## Errores que permanecen en la pantalla

Para errores normales:

```js
const errorProcesado = {
    codigo:error.code || error.status,
    mensaje:error.message,
    errors:error.errors
}
```

Se actualiza el estado:

```js
setter(errorProcesado)
```

Esto permite que los componentes muestren mensajes como:

```js
{
    codigo:400,
    mensaje:"El email ya existe",
    errors:{
        email:"Ya registrado"
    }
}
```

---

# Template de error

El contexto provee:

```js
errorTemplate()
```

que genera una estructura inicial:

```js
{
    codigo: undefined,
    mensaje: undefined,
    errors: undefined
}
```

Esto permite mantener un formato consistente para estados de error en componentes.

Ejemplo:

```js
const [error,setError] =
useState(errorTemplate())
```

---

# Ejemplo completo de flujo

Supongamos que un usuario intenta registrarse con un email ya utilizado.

## Backend

El servicio lanza:

```java
throw new BadRequestException(
    "El email ya existe"
);
```

---

## ErrorHandler

Captura:

```java
@ExceptionHandler(BadRequestException.class)
```

y devuelve:

```json
{
    "code":400,
    "message":"El email ya existe",
    "errors":{}
}
```

---

## Axios

Recibe:

```http
400 BAD REQUEST
```

y transforma:

```js
{
    type:"API_ERROR",
    status:400,
    message:"El email ya existe"
}
```

---

## ErrorContext

Procesa:

```js
setter({
    codigo:400,
    mensaje:"El email ya existe"
})
```

---

## Componente

Muestra:

```jsx
<p>{error.mensaje}</p>
```

Resultado:

```text
El email ya existe
```

---

# Beneficios de esta arquitectura

- Un único punto de manejo de errores.
- Componentes más simples.
- Respuestas homogéneas entre frontend y backend.
- Menos lógica repetida.
- Navegación automática para errores críticos.
- Manejo centralizado de sesiones expiradas.
- Facilita agregar nuevos tipos de errores.