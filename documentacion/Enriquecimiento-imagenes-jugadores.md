# Enriquecimiento de imagenes de jugadores

## Qué hace

Al iniciar la app, se lanza un proceso asíncrono que consulta la API externa [TheSportsDB](https://www.thesportsdb.com) para obtener la URL de la foto de cada jugador y la persiste en la Figurita.
Solo procesa las figuritas que todavía no tienen imagen, y retoma las que quedaron colgadas.

## Flujo

```
* InicializadorDeDatos
-> ServicioDeAgregacionDeDatos.enriquecer()
-> RepositorioFiguritas.buscarPendientes(ttl)

* Para cada figurita:
-> RepositorioFiguritas.reclamarParaProcesamiento(id, ttl)
-> TheSportsDbImagenProveedor.buscarImagen(nombre)
-> RepositorioFiguritas.guardar(figurita)
```

## Lock

El claim se hace con `findAndModify` sobre la colección `figuritas`:

- **Query:** `{_id: X, imagenStatus: null}` o `{imagenStatus: EN_PROCESO, imagenCreadoEn < (now - 10min)}`
- **Update:** `{$set: {imagenStatus: EN_PROCESO, imagenCreadoEn: now}}`

El primer proceso que matchea actualiza `imagenCreadoEn = now`, por lo que una segunda instancia o proceso ya no matchea el TTL y hace skip.

## Rate limiting y reintentos

`TheSportsDbImagenProveedor` usa Resilience4j:

- 1 request cada `60/rpm` segundos (default: 1 req/2s). Evita consumir todos los permisos de golpe porque la api permite como maximo 30 request por min.
- 3 reintentos con 60s de espera, solo ante HTTP 429.
- Si la API no encuentra al jugador, `imagenUrl` queda `null` e `imagenStatus = COMPLETADO` para que no se reintenta en el próximo arranque.
  (Se puede mejorar pero si no se encuentra puede ser que el nombre este mal y conviene revisarlo a mano)

## Normalización de nombres

Los nombres se normalizan antes de armar la URL:

```
"Di María"     → "di_maria"
"N'Golo Kanté" → "ngolo_kante"
```

Usamos el formateo NFD descompone acentuaciones, después se eliminan los caracteres no alfanuméricos y los espacios se reemplazan por `_`.

## Configs

| Propiedad                        | Default           | Descripción             |
|----------------------------------|-------------------|-------------------------|
| `THESPORTSDB_API_KEY`            | `123` (free tier) | API key                 |
| `thesportsdb.rpm`                | `30`              | Requests por minuto     |
| `thesportsdb.retry.max-attempts` | `3`               | Reintentos ante 429     |
| `thesportsdb.retry.wait-seconds` | `60`              | Espera entre reintentos |

## Estado en Figurita

| `imagenStatus` | Significado                                                     |
|----------------|-----------------------------------------------------------------|
| `null`         | Sin procesar                                                    |
| `EN_PROCESO`   | Siendo procesada (o colgada si `imagenCreadoEn` expiró)         |
| `COMPLETADO`   | Procesada (`imagenUrl` puede ser nil si no se encontró imagen) |


## Disclaimer

Hay varias cosas por mejorar. En primer lugar habría de ver como paginar la carga de figuritas porque hoy en día cargamos todas las figuritas en memoria, 
lo cual puede ser costoso. Además podríamos ver que otros campos nos interesaría agregar