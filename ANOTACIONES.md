# Anotaciones de mejoras / bugs UX

## Sugerencias

- **Las sugerencias NO se actualizan en tiempo real al cambiar la colección.**
  Se calculan y persisten en MongoDB en dos momentos: al arrancar el backend y todos los días a las 3am via cron (`Cronjobs.java`). Si un usuario agrega faltantes o repetidas durante el día, sus sugerencias quedan desactualizadas hasta la próxima corrida.
  Opciones de solución:
  1. Regenerar las sugerencias del perfil cada vez que cambia su colección (al guardar faltantes/repetidas). Fix más correcto.
  2. Agregar botón "Actualizar sugerencias" en la pestaña para regenerar manualmente.
  3. Bajar la frecuencia del cron (ej. cada hora) como parche intermedio.
  > **⚠️ Parcialmente resuelto por Agente F** — se implementó la opción 2: endpoint `POST /sugerencias/recalcular` + botón en el panel de admin. Las opciones 1 y 3 siguen pendientes.

## Modelo de datos

- **Refactor: que el `_id` de Figurita sea autogenerado por MongoDB en lugar de un string manual tipo "ARG-10".**
  Hoy `Figurita.id` = `"ARG-10"` (seteado a mano en `FiguritasQatar2022.java`). El campo `numero` (int, ej: `10`) y `seleccion` (ej: `ARGENTINA`) ya existen por separado, por lo que `"ARG-10"` es derivable — no haría falta un campo nuevo.
  
  **Impacto importante:** todos los `@DBRef Figurita` en `Coleccion` (faltantes), `FiguritaIntercambiable`, `Propuesta` (figuritaBuscada / figuritasOfrecidas), `Subasta` y `Sugerencia` almacenan el string `"ARG-10"` como referencia. Cambiar el `_id` a ObjectId requiere:
  1. Migrar los DBRefs en todas esas colecciones en MongoDB.
  2. Actualizar `FiguritasQatar2022` y el `InicializadorDeDatos` para no setear `.id(...)`.
  3. Actualizar el frontend: los requests que hoy envían `fig_id: "ARG-10"` pasarían a enviar el ObjectId.
  4. El campo derivado `"ARG-10"` puede generarse en el DTO si se necesita para mostrar/buscar.
  > **❌ No resuelto** — impacto demasiado amplio, fuera del scope del plan de remediación.

## Notificaciones

- **Bug: la notificación de "nueva figurita disponible" muestra el ID en lugar del nombre del jugador.**
  El texto generado es `"Nueva figurita disponible, Numero: BRA-10, Cantidad: 1"`. Debería usar `figurita.getJugador()` en lugar de `figurita.getId()`.
  Fix de una línea: `backend/src/main/java/app/servicios/ServicioColeccion.java:73`
  Ejemplo del texto correcto: `"Nueva figurita disponible: Neymar Jr. (BRA-10), Cantidad: 1"`
  > **❌ No resuelto** — fuera del scope de todos los agentes.

## Naming / UX General

- **Considerar renombrar "Intercambios" → "Propuestas" en la navegación.**
  La sección muestra propuestas en todos sus estados (pendiente, aceptada, rechazada, cancelada), no solo intercambios concretados. "Propuestas" es más preciso y evita colisión semántica con el término `MetodoIntercambio` del modelo. Requiere cambio de label en el navbar y en las rutas del frontend.
  > **❌ No resuelto** — cambio estético, no era parte del plan de remediación.

## Intercambios

- **Bug: `agregarRepetida` no mergea métodos al acumular cantidades.**
  `Coleccion.agregarRepetida()` solo suma `cantidadExistente` cuando ya existe una entrada para esa figurita, pero ignora los `metodos` de la nueva entrada. Si el usuario agrega Messi ×1 para SUBASTA y luego ×1 para INTERCAMBIO, queda `cantidadExistente=2` pero `metodos=[SUBASTA]` — el método INTERCAMBIO nunca se agrega. Resultado: la figurita aparece en la lista pero con "Sin stock" para intercambio.
  Fix: en `agregarRepetida`, si la entrada ya existe, también mergear los métodos (`addAll` + dedup).
  Archivo: `backend/src/main/java/app/model/entities/Coleccion.java:46`
  > **❌ No resuelto** — no estaba en ningún plan de remediación.

- **UX: "Proponer intercambio" muestra "No hay resultados" en tus repetidas sin explicación.**
  El `SelectorRepetidas` en `CrearPropuestaIntercambio` pasa `perfilId={figurita.perfil_id}` (el dueño de la figurita ofrecida). El backend usa ese ID para resolver los *faltantes del otro usuario* y filtra las repetidas del usuario logueado, mostrando **solo las que el otro necesita**. Si ninguna de tus repetidas está en los faltantes del otro → "No hay resultados", sin ninguna explicación al usuario.
  Fix: mostrar un mensaje claro del tipo "Ninguna de tus repetidas coincide con lo que este usuario necesita", o reconsiderar si el filtrado cruzado es la UX correcta (permitir ofrecer cualquier repetida y que el otro decida).
  Archivos: `frontend/src/views/public/crear-propuesta-intercambio/crear-propuesta-intercambio.jsx:62`, `backend/src/main/java/app/repositories/impl/RepositorioColeccionesMongo.java:176`
  > **✅ Resuelto por Agente A** — `SelectorRepetidas` acepta `mensajeVacio` personalizado; además el `perfilId` ahora usa la sesión propia en lugar del dueño de la figurita, por lo que el filtrado ya no vacía la lista incorrectamente.

- **Bug UX: se puede llegar al form "Proponer intercambio" con una figurita que no está en tus faltantes. (User Story 5 no funciona; US 9 tampoco; US 8 parcialmente)**
  El backend valida en `ServicioPropuesta.crearPropuesta()` que la figurita buscada esté en tus faltantes, y devuelve 400 si no. El frontend no previene el acceso al formulario, entonces el usuario completa todo y recibe el error recién al enviar.
  Fix: deshabilitar el botón de "Proponer intercambio" si la figurita no está en los faltantes del usuario, o agregar un aviso temprano.
  Archivo: `backend/src/main/java/app/servicios/ServicioPropuesta.java:72`
  > **⚠️ Parcialmente resuelto por Agente A** — el fix del `perfilId` elimina el 400 en cascada más común; el error ahora muestra texto claro gracias a `errorContext`. Falta: deshabilitar el botón preventivamente en el frontend antes de que el usuario intente enviar.

- **Bug/Gap: al concretar un intercambio, la figurita recibida no aparece en "Mis figuritas".**
  `Propuesta.ejecutarIntercambio()` elimina la figurita de `faltantes` y descuenta la repetida entregada, pero **no agrega la figurita recibida a ningún lugar de la colección**. El modelo solo tiene `faltantes` y `repetidas` — no existe un "álbum" de figuritas ya conseguidas. La figurita queda en un limbo: ya no es faltante pero tampoco es visible en la UI.
  Opciones:
  1. Agregar un concepto de "álbum" (figuritas conseguidas, no intercambiables) al modelo de `Coleccion`.
  2. Mostrar la figurita como repetida con cantidad 1 y sin métodos de intercambio activos.
  Archivo: `backend/src/main/java/app/model/entities/Propuesta.java:129`
  > **❌ No resuelto** — requiere nuevo concepto "álbum" en el modelo, fuera del scope del plan.

- **Bug: se puede calificar a un usuario aunque el intercambio no se haya completado correctamente.**
  La calificación debería estar habilitada solo después de un intercambio realmente concretado (estado ACEPTADO con colecciones actualizadas). Dado que el flujo de intercambio tiene los problemas descriptos arriba, el hecho de que la calificación funcione igual sugiere que la validación no está atada al estado real del intercambio.
  > **⚠️ Parcialmente resuelto por Agente B** — el botón "Calificar" ahora se deshabilita correctamente tras calificar (`setYaCalificado(true)` en `intercambio-card.jsx`) y no permite duplicados (índice único + fix String/ObjectId en `RepositorioCalificacionesMongo`). Falta: atar la habilitación del botón al estado real de las colecciones actualizado.

- **Bug: el toast de error se muestra múltiples veces al hacer click en "Enviar propuesta".**
  Confirmado en capturas: el error aparece 4 veces apilado. El botón usa `disabled={enviando}` pero hay una ventana entre clicks rápidos y el seteo del estado `enviando`, permitiendo múltiples requests simultáneos. Fix: usar una ref o deshabilitar el botón sincrónicamente antes del await.
  Archivo: `frontend/src/views/public/crear-propuesta-intercambio/useCrearPropuesta.js` (revisar manejo de `enviando`)
  > **⚠️ Parcialmente resuelto por Agente A** — se garantiza que el toast siempre muestra texto legible (`err.mensaje || fallback`). Falta: el race condition sigue presente; hay que cambiar `useState` por `useRef` para `enviando` y leer/escribir la ref sincrónicamente antes del `await`.

## Mis Figuritas

- **"Nueva faltante": mostrar card del jugador en lugar del nombre en texto plano.**
  Cuando se busca una figurita en el formulario de nueva faltante, el resultado se muestra como nombre + código en texto plano dentro de un recuadro punteado. Debería reemplazarse por la card del jugador (la misma que se usa en el resto de la app).
  > **❌ No resuelto** — mejora de UX menor, no estaba en el plan.

- **Bug: el botón "Ver subasta" en Explorar lleva a la subasta equivocada.**
  La card de una figurita en Explorar muestra correctamente al dueño (ej: Lucas), pero el botón "Ver subasta" redirige a la subasta de otro usuario (ej: valentin). El frontend probablemente resuelve el link buscando cualquier subasta activa para esa figurita por ID, sin filtrar por perfil dueño, y toma la primera que encuentra. La subasta mostrada en el detalle no corresponde al usuario de la card.
  Archivos: revisar cómo se construye el link a la subasta en la card de Explorar.
  > **❌ No resuelto** — no fue abordado en esta sesión.

- **UX: en "Crear oferta" las repetidas no muestran por qué no aparecen o qué método necesitan.**
  Al ofertar en una subasta, el selector solo muestra repetidas marcadas como SUBASTA. Si el usuario tiene la figurita requerida pero con método INTERCAMBIO, no aparece en la lista ni como bloqueada, sin ninguna explicación. El usuario intuye que debería funcionar pero no entiende el motivo del rechazo.
  También hay un cross-filter por `perfilId` del dueño de la subasta que puede vaciar la lista aunque el usuario tenga repetidas válidas.
  Fix de UX: mostrar las figuritas excluidas con una etiqueta explicativa ("tenés esta figurita pero está marcada solo para intercambio") o mostrar un mensaje claro cuando el método no coincide.
  Archivos: `frontend/src/views/public/crear-oferta/crear-oferta.jsx`, `frontend/src/components/ui/selector-repetidas/selector-repetidas.jsx`
  > **⚠️ Parcialmente resuelto por Agente A** — `SelectorRepetidas` ahora acepta `mensajeVacio` personalizado, lo que permite mostrar un mensaje explicativo cuando la lista está vacía. Falta: mostrar las figuritas excluidas por método con una etiqueta explicativa (actualmente se ocultan sin aviso).

## Subastas

- **"Crear subasta": la lista de repetidas muestra figuritas sin stock.**
  En el paso 1 ("Elegí la figurita a subastar") aparecen todas las repetidas, incluyendo las que tienen stock 0 ("Sin stock"). Hay que definir si se filtran solo las que tienen stock disponible, o solo las que fueron marcadas explícitamente para subasta. Mostrar opciones no subastaables confunde al usuario.
  > **❌ No resuelto** — no fue abordado.

- **UX: el botón "Adjudicar" no es suficientemente visible ni intuitivo.**
  En "Mis subastas", el botón para adjudicar una oferta aparece como un botón pequeño de estilo secundario (outline) al lado del ✕. El usuario no entiende rápidamente que esa es la acción principal para cerrar la subasta a favor de un ofertante.
  Fix: darle más jerarquía visual al botón (color primario, más grande), y/o agregar un label explicativo como "Adjudicar subasta a esta oferta" o un tooltip.
  > **⚠️ Parcialmente resuelto por merge a developer** — se implementó la funcionalidad de adjudicar desde `ver-subasta.jsx` (el dueño ahora puede adjudicar directamente). Falta: mejorar la jerarquía visual del botón (tamaño, color primario, tooltip).

- **Crear subasta desde "Mis Figuritas" no activa la subasta automáticamente.**
  Al crear una subasta desde esa pestaña, queda en estado inactivo. Para activarla hay que ir manualmente a "Mis Subastas" e iniciarla desde ahí.
  Opciones: activarla automáticamente al crearla, o redirigir al usuario a "Mis Subastas" al finalizar la creación.
  > **❌ No resuelto** — no fue abordado.
