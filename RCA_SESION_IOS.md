# RCA — Sesión no persistía en iOS

## Descripción del problema

Los usuarios con iPhone no podían iniciar sesión en la aplicación. Tras completar el flujo de login exitosamente, cualquier navegación a una ruta protegida resultaba en el error _"No se encontró una sesión"_ y el usuario era devuelto a la pantalla de login. El problema afectaba a Safari y Chrome iOS por igual.

En desktop (Chrome, Firefox) y Android el comportamiento era correcto.

## Causa raíz

### Mecanismo de sesión

La autenticación se implementa mediante una cookie `httpOnly` llamada `token`, que contiene un JWT firmado. Al hacer login, el backend responde con un `Set-Cookie` que el browser debe almacenar y enviar en todas las requests subsiguientes.

La cookie estaba configurada con `SameSite=None; Secure`, pensada para escenarios cross-site.

### Por qué fallaba en iOS

El frontend vive en `figunet.onrender.com` y el backend en `figunet-api.onrender.com`. Aunque visualmente parecen subdominios del mismo sitio, `onrender.com` figura en la [Public Suffix List](https://publicsuffix.org/) — el mismo registro que contiene `.com`, `.ar`, `.github.io`. Esto significa que el browser los trata como **sitios completamente distintos**, igual que si fueran `google.com` y `facebook.com`. La cookie es, por definición, **cross-site (de terceros)**.

En iOS, Apple obliga a todos los browsers a usar el motor WebKit (incluso Chrome iOS es WebKit por dentro). WebKit implementa **ITP (Intelligent Tracking Prevention)**, una política de privacidad que bloquea cookies de terceros por defecto para evitar el rastreo cross-site de usuarios.

Como resultado, el `Set-Cookie` que devolvía el backend al hacer login era **silenciosamente ignorado** por WebKit. Las requests subsiguientes (como `GET /yo` para verificar la sesión) viajaban sin cookie → el backend respondía 401 → el frontend mostraba _"No se encontró una sesión"_.

### Por qué no fallaba en desktop

Chrome y Firefox en desktop tienen políticas más permisivas con cookies `SameSite=None` siempre que vengan con `Secure`. No implementan ITP. Por eso el flujo funcionaba correctamente fuera de iOS.

## Impacto

- **Funcionalidad afectada:** login, sesión y toda la app (rutas protegidas inaccesibles).
- **Usuarios afectados:** todos los usuarios con iPhone — la aplicación era inutilizable desde iOS.
- **Plataformas no afectadas:** desktop (Chrome, Firefox, Edge) y Android.

## Resolución

### Proxy same-origin en Nginx

Se implementó un proxy dentro del contenedor del frontend. El browser ahora solo habla con `figunet.onrender.com` — nunca ve el dominio del backend. Las requests a `/api/*` son interceptadas por Nginx y reenviadas internamente a `figunet-api.onrender.com`, quitando el prefijo `/api`.

```
Browser → figunet.onrender.com/api/login
              ↓ Nginx (proxy interno)
          figunet-api.onrender.com/login
              ↓
          Set-Cookie: token=... (asociado a figunet.onrender.com)
```

Desde la perspectiva del browser, la cookie la setea `figunet.onrender.com` → es **first-party** → WebKit la acepta y la envía en todas las requests siguientes.

La URL del backend se parametriza vía la variable de entorno `BACKEND_URL`, resuelta por `envsubst` al arrancar el contenedor. Esto permite usar el mismo artefacto Docker en distintos entornos (staging, producción) cambiando solo esa variable en Render.

### Cambio en la cookie

Se cambió `SameSite=None` → `SameSite=Lax` en login y logout. Con el esquema same-origin este valor es el correcto: es más restrictivo (protege contra algunos vectores CSRF) y es el default moderno recomendado.

## Archivos modificados

| Archivo | Cambio |
|---|---|
| `frontend/nginx.conf` | `location /api/` con proxy al backend y `proxy_ssl_server_name on` |
| `frontend/Dockerfile` | `nginx.conf` copiado a `/etc/nginx/templates/` para soporte de `envsubst` |
| `backend/.../ControladorSesion.java` | `SameSite=None` → `SameSite=Lax` en login y logout |
| `frontend/src/services/api.js` | `baseURL` hardcodeado a `/api` |
| `frontend/vite.config.js` | Proxy de Vite `/api` → `http://localhost:8080` para desarrollo local |
