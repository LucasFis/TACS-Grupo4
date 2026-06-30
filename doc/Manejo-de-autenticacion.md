# Manejo de autenticación

Este documento describe la arquitectura y el flujo de autenticación implementado entre frontend y backend utilizando **JWT almacenado en cookies HttpOnly**.

Los objetivos principales son:

- Centralizar la gestión de sesiones.
- Mantener la autenticación transparente para el usuario.
- Evitar exponer el token al frontend.
- Validar automáticamente la sesión en cada request.
- Invalidar sesiones expiradas de forma automática.
- Sincronizar el estado del usuario entre frontend y backend.

---

# Arquitectura general

El flujo general de autenticación es el siguiente:

```text
Usuario inicia sesión
        ↓
Backend valida credenciales
        ↓
Backend genera JWT
        ↓
JWT se guarda en cookie HttpOnly
        ↓
Browser envía cookie automáticamente
        ↓
JwtFilter valida el token
        ↓
Se procesa la petición
        ↓
Frontend mantiene el estado de sesión
```

---

# Backend

El backend se divide en cuatro componentes principales:

1. `ControladorSesion`
2. `ServicioSesion`
3. `ServicioJwt`
4. `JwtFilter`

---

# ControladorSesion

```java
@RestController
public class ControladorSesion
```

Es el punto de entrada de las operaciones relacionadas con la sesión.

Responsabilidades:

- Iniciar sesión
- Obtener datos de sesión
- Cerrar sesión

---

## Login

Endpoint:

```http
POST /login
```

Recibe:

```json
{
    "nombre":"usuario",
    "contrasenia":"123456"
}
```

Proceso:

```text
Cliente envía credenciales
        ↓
ServicioSesion valida usuario
        ↓
ServicioJwt genera token
        ↓
Se crea cookie HttpOnly
        ↓
Cookie enviada al navegador
```

Código simplificado:

```java
String token = servicioSesion.login(request);
```

Creación de cookie:

```java
ResponseCookie.from("token", token)
    .httpOnly(true)
    .secure(true)
    .sameSite("None")
    .path("/")
    .maxAge(Duration.ofHours(12))
```

---

## Configuración de seguridad de la cookie

| Configuración | Función |
|-------------|----------|
| `httpOnly(true)` | Impide acceso desde JavaScript |
| `secure(true)` | Solo se envía por HTTPS |
| `sameSite("None")` | Permite envío entre frontend y backend |
| `path("/")` | Disponible para toda la aplicación |
| `maxAge(12h)` | Duración de sesión |

---

## Obtener usuario autenticado

Endpoint:

```http
GET /yo
```

Obtiene automáticamente el token desde cookies:

```java
@CookieValue("token")
```

Luego:

```java
SesionDto dto =
servicioJwt.obtenerSesion(token);
```

Respuesta:

```json
{
    "usuarioId":"123",
    "rol":"ADMIN",
    "perfilId":"456",
    "colId":"789"
}
```

---

## Cerrar sesión

Endpoint:

```http
DELETE /sesion
```

No elimina el JWT del servidor (porque JWT es stateless), sino que elimina la cookie:

```java
ResponseCookie.from("token", "")
    .maxAge(0)
```

Esto hace que el navegador elimine inmediatamente la sesión.

---

# ServicioSesion

```java
@Service
public class ServicioSesion
```

Responsable de validar credenciales y generar sesiones.

---

## Flujo de login

Proceso:

```text
Buscar usuario
      ↓
Comparar contraseña
      ↓
Buscar perfil
      ↓
Generar JWT
```

Implementación simplificada:

```java
Usuario usuario =
repoUsuario.buscarPorNombre(
    request.nombre()
);

boolean coincide =
passwordEncoder.matches(
    request.contrasenia(),
    usuario.getContrasenia()
);

if(!coincide){
    throw new UsuarioException(
        "Credenciales invalidas"
    );
}

return servicioJwt.generarToken(
    usuario,
    perfil
);
```

---

## Validación de contraseña

Las contraseñas se validan mediante:

```java
BCryptPasswordEncoder
```

Ejemplo:

```java
passwordEncoder.matches(
    passwordIngresada,
    passwordEncriptada
)
```

Esto evita almacenar contraseñas en texto plano.

---

# ServicioJwt

```java
@Service
public class ServicioJwt
```

Responsable de:

- Generar JWT
- Firmar JWT
- Validar JWT
- Extraer información

---

# Generación del token

El token almacena:

```java
.claim("usuarioId", usuario.getId())
.claim("rol", usuario.getRol().toString())
.claim("perfilId", perfil.getId())
.claim("colId", perfil.getColeccion().getId())
```

Información contenida:

| Campo | Descripción |
|---------|-------------|
| usuarioId | Id del usuario |
| rol | Rol del usuario |
| perfilId | Perfil asociado |
| colId | Colección asociada |

---

## Expiración

El token tiene duración:

```java
12 horas
```

Configurado mediante:

```java
.setExpiration(
    new Date(
        System.currentTimeMillis()
        + 1000*60*60*12
    )
)
```

---

## Firma del token

Se utiliza:

```java
HS256
```

junto a una clave secreta:

```java
.signWith(
    getSignKey(),
    SignatureAlgorithm.HS256
)
```

La firma permite detectar modificaciones en el token.

---

## Validación

Cada request protegido ejecuta:

```java
servicioJwt.validarToken(token)
```

La validación controla:

- Expiración
- Integridad
- Firma

---

# JwtFilter

```java
@Component
public class JwtFilter
```

Es un filtro ejecutado una vez por request.

Su función es proteger endpoints privados.

---

## Flujo del filtro

```text
Llega request
      ↓
Buscar cookie token
      ↓
Validar JWT
      ↓
Token válido
      ↓
Continuar request
```

Si falla:

```text
Llega request
      ↓
Token inválido
      ↓
UnauthorizedException
      ↓
ErrorHandler responde 401
```

---

## Endpoints públicos

El filtro excluye:

```java
return path.startsWith("/login")
    || path.startsWith("/usuarios")
    || path.startsWith("/figuritas")
    || path.startsWith("/ping")
    || path.startsWith("/sesion");
```

Estos endpoints pueden utilizarse sin autenticación.

---

## Obtención del token

El token se busca automáticamente en cookies:

```java
private String obtenerToken(
    HttpServletRequest request
)
```

Proceso:

```text
Obtener cookies
      ↓
Buscar cookie "token"
      ↓
Retornar valor
```

Si no existe:

```java
throw new UnauthorizedException(
    "No se encontró el token"
);
```

---

## Manejo de errores dentro del filtro

Si ocurre:

```java
JwtException
```

o:

```java
UnauthorizedException
```

el filtro utiliza:

```java
errorHandler.resolveException(...)
```

para reutilizar el manejo global de errores.

Esto garantiza respuestas consistentes.

---

# Frontend

El frontend utiliza dos componentes principales:

1. `AuthContext`
2. `sesionService`

---

# AuthContext

```js
const AuthContext =
createContext()
```

Responsabilidades:

- Mantener usuario actual
- Persistir sesión
- Verificar autenticación
- Cerrar sesión
- Escuchar expiraciones automáticas

---

# Inicialización

Al iniciar la aplicación:

```js
const storedUser =
localStorage.getItem("sesion")
```

Si existe:

```js
setUser(
    JSON.parse(storedUser)
)
```

Esto evita perder el estado al refrescar la página.

---

## Verificación de sesión

Al cargar:

```js
useEffect(()=>{
    verificarSesion()
},[])
```

Ejecuta:

```js
buscarUsuario()
```

que realiza:

```http
GET /yo
```

Esto permite validar que el token almacenado siga siendo válido.

---

## Estado de autenticación

```js
const tieneSesion =
user !== undefined
```

Puede utilizarse para:

```jsx
{
    tieneSesion &&
    <NavbarPrivado/>
}
```

---

## Actualizar usuario

```js
updateUser(fields)
```

Permite modificar parcialmente datos del usuario:

```js
updateUser({
    nombre:"Juan"
})
```

Internamente:

```js
const updatedUser = {
    ...user,
    ...fields
}
```

---

## Cerrar sesión manual

```js
cerrarSesion()
```

Proceso:

```text
Eliminar usuario
      ↓
Eliminar localStorage
      ↓
Llamar DELETE /sesion
      ↓
Redirigir al inicio
```

---

# SesionService

Archivo:

```js
sesionService.js
```

Centraliza llamadas relacionadas con autenticación.

Funciones:

---

## iniciarSesion

```js
POST /login
```

Ejemplo:

```js
await iniciarSesion({
    nombre,
    contrasenia
})
```

---

## buscarUsuario

```js
GET /yo
```

Obtiene datos del usuario autenticado.

---

## logout

```js
DELETE /sesion
```

Finaliza sesión.

---

# Manejo automático de expiración

Existe un interceptor global de Axios que detecta:

```http
401 Unauthorized
```

Proceso:

```text
JWT expira
      ↓
Backend devuelve 401
      ↓
Interceptor ejecuta logout
      ↓
Dispara evento logout
      ↓
AuthContext escucha evento
      ↓
Elimina sesión
      ↓
Redirecciona al inicio
```

Código simplificado:

```js
window.dispatchEvent(
    new Event("logout")
)
```

Listener:

```js
window.addEventListener(
    "logout",
    listener
)
```

---

# Flujo completo de autenticación

## Inicio de sesión

```text
Usuario ingresa credenciales
        ↓
POST /login
        ↓
ServicioSesion valida usuario
        ↓
ServicioJwt genera token
        ↓
Cookie HttpOnly enviada
        ↓
Browser guarda cookie
        ↓
Frontend consulta /yo
        ↓
Usuario autenticado
```

---

## Navegación posterior

```text
Usuario realiza request
        ↓
Browser envía cookie automáticamente
        ↓
JwtFilter valida token
        ↓
Request continúa
```

---

## Expiración automática

```text
JWT expira
      ↓
JwtFilter rechaza request
      ↓
401 Unauthorized
      ↓
Interceptor detecta error
      ↓
Logout automático
      ↓
Usuario vuelve al inicio
```

---

# Beneficios de esta arquitectura

- El token nunca es accesible desde JavaScript.
- No se almacena JWT en LocalStorage.
- Manejo automático de expiración.
- Sesiones consistentes entre frontend y backend.
- Menor riesgo ante ataques XSS.
- Validación centralizada.
- Frontend desacoplado del mecanismo interno del JWT.
- Permite agregar permisos y roles fácilmente.