# Circuit Breaker: Race Conditions en peticiones HTTP

## Dependencia

`@tanstack/react-query` **v5.101.2**

---

## Problema

Cuando un usuario navega rápidamente entre tabs o cambia filtros antes de que las peticiones anteriores se resuelvan, ocurre un **race condition**: la respuesta más lenta sobreescribe el estado con datos obsoletos.

### Ejemplo concreto (Intercambios)

```
1. Usuario en "Recibidas" → se dispara petición para recibidos
2. Cambia a "Enviadas"    → se dispara petición para enviadas
3. Toca filtro "Canceladas" → se dispara petición para enviadas canceladas

Si la petición 1 es la más lenta:
→ Se resuelve la 3° (canceladas) → estado muestra canceladas ✅
→ Se resuelve la 1° (recibidos)  → SOBREESCRIBE el estado ❌
→ Resultado: se muestran recibidos como si fueran canceladas
```

### Patrón causal

```js
// ANTES (vulnerable a race conditions)
const [data, setData] = useState([])
const [loading, setLoading] = useState(true)

const cargar = async () => {
  setLoading(true)
  const res = await fetch(filtros)
  setData(res)  // ← sobreescribe sin importar si los filtros cambiaron
  setLoading(false)
}

useEffect(() => { cargar() }, [filtros])
```

---

## Solución: TanStack Query

[TanStack Query](https://tanstack.com/query) gestiona automáticamente el ciclo de vida de las peticiones:

| Mecanismo | Cómo resuelve el race condition |
|---|---|
| **Query Keys** | Cada combinación de `queryKey` es una query distinta. Si la key cambia, se descarta la anterior |
| **AbortController** | El `signal` se pasa a axios. Cuando la queryKey cambia, se aborta la petición HTTP anterior |
| **Deduplicación** | Si dos componentes piden la misma queryKey, solo se hace una petición |
| **Cache** | Los datos de queries anteriores quedan en cache por `staleTime` |

### Patrón migrado

```js
// DESPUÉS (protegido contra race conditions)
const { data, isLoading } = useQuery({
  queryKey: ['propuestas', { pagina, estado, tipo }],
  queryFn: ({ signal }) => buscarPropuestas({ pagina, estado, tipo }, signal),
  onError: (error) => {
    if (error.code === 'ERR_CANCELED') return  // abort silencioso
    showToast(handleError(error, () => {}), 'error')
  },
})
```

---

## Cambios en la arquitectura

### 1. Service layer: soporte para `signal`

Cada función de service que se usa en `useQuery` acepta un segundo parámetro `signal`:

```js
// ANTES
export const buscarPropuestas = async (filtros) => {
  const { data } = await api.get(URL, { params: filtros })
  return data
}

// DESPUÉS
export const buscarPropuestas = async (filtros, signal) => {
  const { data } = await api.get(URL, { params: filtros, signal })
  return data
}
```

Services modificados: `propuestasService`, `explorarService`, `administradorService`, `coleccionService`, `subastasService`, `notificacionesService`, `perfilService`.

### 2. Infraestructura: QueryClientProvider

```jsx
// main.jsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </StrictMode>,
)
```

### 3. TabsContainer: key forzada

```jsx
// ANTES
{ActiveComponent && (<ActiveComponent {...activeTab.props} />)}

// DESPUÉS
{ActiveComponent && (<ActiveComponent key={activeKey} {...activeTab.props} />)}
```

Cuando el usuario cambia de tab, `key={activeKey}` fuerza el remount del componente, reseteando todo el estado local.

### 4. Invalidación de cache

Para acciones mutantes (aceptar, rechazar, cancelar) que deben refrescar una lista:

```js
const queryClient = useQueryClient()

const onActualizado = () => {
  queryClient.invalidateQueries({ queryKey: ['propuestas'] })
}
```

---

## Archivos modificados

### Services (signal)

| Archivo | Funciones |
|---|---|
| `propuestasService.js` | `buscarPropuestas` |
| `explorarService.js` | `explorarFiguritas` |
| `administradorService.js` | `obtenerEstadisticas` |
| `coleccionService.js` | `buscarFaltantes`, `buscarRepetidas` |
| `subastasService.js` | `buscarSubastas` |
| `notificacionesService.js` | `obtenerNotificaciones` |
| `perfilService.js` | `buscarPerfil`, `buscarContadores`, `buscarCalificaciones` |

### Componentes migrados (useQuery)

| Componente | Antes | Después |
|---|---|---|
| `propuestas-tab.jsx` | `useEffect` + `useState` | `useQuery` |
| `useFiguritas.js` | `useEffect` + 5 `useState` | `useQuery` (retorna misma forma) |
| `use-estadisticas-admin.js` | `useEffect` + `useState` | `useQuery` con `isLoading`/`isFetching` |
| `faltantes.jsx` | `useEffect` + `useState` | `useQuery` |
| `repetidas.jsx` | `useEffect` + `useState` | `useQuery` + `setQueryData` |
| `participo.jsx` | `useEffect` + refresh counter | `useQuery` + `invalidateQueries` |
| `mis-subastas.jsx` | `useEffect` + refresh counter | `useQuery` + `invalidateQueries` |
| `notificaciones.jsx` | `useEffect` + `useState` | `useQuery` |
| `usePerfil.js` | `useEffect` + 2 `await` secuenciales | 2 `useQuery` en paralelo |
| `useCalificaciones.js` | `useEffect` + `useState` | `useQuery` |

### Infraestructura

| Archivo | Cambio |
|---|---|
| `main.jsx` | `QueryClientProvider` envuelve la app |
| `tabs-container.jsx` | `key={activeKey}` en componente activo |

---

## Manejo de errores con useQuery

Los errores de `useQuery` se manejan vía `onError`, manteniendo la compatibilidad con `ErrorContext` y `ToastContext`:

```js
const { handleError } = useError()
const { showToast } = useToast()

const { data } = useQuery({
  queryKey: ['recurso', params],
  queryFn: ({ signal }) => fetchRecurso(params, signal),
  onError: (error) => {
    if (error.code === 'ERR_CANCELED') return  // abort de TanStack Query
    showToast(handleError(error, () => {}), 'error')
  },
})
```

El check `error.code === 'ERR_CANCELED'` filtra los aborts automáticos de TanStack Query (cuando el usuario cambia de tab/filtro rápidamente), que no son errores reales.

---

## Componentes NO migrados

Los componentes que solo hacen peticiones en event handlers (`onClick`, `onSubmit`) no necesitan `useQuery`:

| Componente | Razón |
|---|---|
| `ver-subasta.jsx` | Fetch único al montar |
| `ver-intercambio.jsx` | Fetch único + acciones con confirmación |
| `crear-oferta.jsx` | `Promise.all` al montar |
| `editar-oferta.jsx` | Fetch al montar, submit con loading guard |
| Componentes de creación | Solo usan services en submits |
| Login/Registrar | Solo usan services en submits |
