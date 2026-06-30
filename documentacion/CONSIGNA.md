# TP TACS 2026-C1

## Introduccion

El objetivo del TP es desarrollar una aplicación que permita a los usuarios intercambiar figuritas del Mundial.

La aplicación funcionará de modo stand alone, y estará publicada en la nube para ser accedida.

El TP consta de diversas entregas en las cuales de forma iterativa e incremental se irán agregando funcionalidades a la aplicación.

## Recomendaciones Generales

- Enfocarse en los requerimientos de cada entrega. (Se puede hacer de más pero no de menos)
- Utilizar al ayudante para validar decisiones de diseño y consultar arquitectura, frameworks, etc.
- Dividir en forma clara en el equipo las historias de cada entrega para atacarlas en paralelo donde sea posible.
- Utilizar alguna herramienta para la gestión de tareas (Scrummy, Trello, Issues de Github)
- Para bajar el riesgo de las futuras entregas aprovechar el tiempo de entregas anteriores para investigar las tecnologías.
- De ser necesario utilizar al ayudante como facilitador, en cuestiones técnicas y de organización → El rol del ayudante NO es simplemente el de corregir, sino dar soporte al equipo durante todo el proceso en cuestiones técnicas y metodológicas.

## Uso de IA

El uso de herramientas de IA no solo no está prohibido, sino que recomendamos activamente su utilización durante el desarrollo del TP, tanto para explorar alternativas como para destrabar problemas, validar decisiones de diseño, acelerar la implementación o mejorar la calidad de los entregables.

Sin embargo, todo lo producido y entregado es responsabilidad exclusiva del grupo. Esto implica que cada integrante deberá comprender, validar y poder explicar cada decisión tomada, cada diseño propuesto y cada línea de código presentada, independientemente de haber utilizado asistencia de IA.

Adicionalmente, el grupo deberá documentar en el README.me/Wiki qué herramientas de IA utilizó, de qué forma las utilizó y para qué tipo de tareas fueron empleadas. No buscamos una enumeración exhaustiva de prompts, sino una descripción clara y honesta del uso realizado y del criterio con el que se integró la asistencia de IA al proceso de desarrollo.

## Objetivo de la aplicación

La aplicación tiene como objetivo permitir a los usuarios publicar figuritas repetidas de su colección y ofrecerlas para intercambio. A su vez, los usuarios podrán registrar las figuritas que les faltan, buscar coincidencias con otros usuarios, hacer propuestas de intercambio y concretar operaciones dentro de la plataforma.

Además del intercambio directo, la plataforma incorporará funcionalidades avanzadas como subastas de figuritas, sugerencias automáticas de intercambio, reputación entre usuarios y alertas ante nuevas oportunidades relevantes.

La aplicación debe poder escalar para manejar gran cantidad de usuarios concurrentes y colecciones extensas. Los dueños del proyecto buscan una solución moderna, sin colas virtuales ni mecanismos manuales.

## User Stories

1. Como usuario quiero publicar una figurita que quiero intercambiar, especificando:
    - a. Número de figurita
    - b. Selección, equipo o categoría
    - c. Jugador o descripción
    - d. Cantidad disponible
    - e. Si la figurita puede participar en intercambio directo o subasta
2. Como usuario quiero registrar las figuritas que me faltan para completar mi colección.
3. Como usuario quiero poder buscar figuritas disponibles, aplicando filtros por número, selección, jugador, etc.
4. Como usuario quiero recibir sugerencias automáticas de intercambio en función de mis faltantes y las figuritas repetidas de otros usuarios.
5. Como usuario quiero hacer una propuesta por una figurita, pudiendo ofrecer una o más figuritas de mi colección.
6. Como usuario quiero publicar una figurita en subasta, definiendo reglas como duración y condiciones mínimas para adjudicarla.
7. Como usuario quiero ofertar por una subasta activa utilizando figuritas de mi colección.
8. Como usuario quiero ver mis figuritas publicadas, propuestas enviadas y recibidas, subastas activas y el estado de cada operación.
9. Como usuario quiero aceptar o rechazar propuestas recibidas.
10. Como usuario quiero calificar a otro usuario luego de concretar un intercambio para construir un sistema de reputación.
11. Como usuario quiero recibir alertas cuando aparezca una figurita que me falta, cuando una subasta de mi interés esté por finalizar o cuando reciba una nueva propuesta.
12. Como administrador quiero ver estadísticas de uso y actividad de la plataforma.

## Requerimientos no funcionales

Los requerimientos no funcionales no solo son importantes para aprobar el TP sino que están directamente relacionados con la filosofía y objetivos de la materia. La calidad no se negocia.

### Técnicos

- No es el objetivo del TP trabajar sobre la creación o autenticación de usuarios. Dicho esto, es importante poder diferenciar de alguna forma los mismos para poder atacar los casos de uso.
- Se debe utilizar Github/GitLab como SCM.
- Los tests son parte del código. Un caso de uso que no está debidamente testeado, tampoco está completo.
- Todos los métodos no triviales deben tener su correspondiente doc (ej: javadoc) explicando su función, forma de uso y cualquier otra información relevante.
- Se debe incluir en el README.me/Wiki cómo levantar la aplicación y cualquier decisión respecto del código o las soluciones utilizadas.
- La aplicación debe ser capaz de correrse desde Maven/Gradle/SBT/Node, el comando a correr debe iniciar la aplicación dentro de un Docker container.
- Se debe usar docker-compose para definir el conjunto de aplicación + db + network de modo tal que se pueda correr todo con un solo comando. Esto es obligatorio para modo local, si en la nube se va a utilizar alguna SaaS DB, entonces para el deploy solo es necesario el Docker container de la main app.
- La APP tiene que cumplir con requerimientos mínimos de seguridad (manejo de contraseñas, recursos externos, etc.)
- Las claves deben ser guardadas de forma correcta.
- En caso de existir, las API keys NUNCA debe ser expuestas al usuario, ni estar en el repositorio. Documentar cómo disponibilizarán las keys a la aplicación.
- La aplicación debe soportar un load test, se utilizara alguna tool como Vegeta, Wrk, etc.
- La aplicación debe ser portable, requiriendo solamente de Gradle/Maven/SBT/Node + Docker para su prueba y evaluación.
- La aplicación debe tener una interfaz de usuario fácil de utilizar, a elegir entre frontend o integración por telegram.

### UI

- Si bien se espera algo sencillo, la aplicación debe tener un frontend amigable. Recomendamos seguir los lineamientos de https://material.io/
- Utilizar algun framework CSS (Bootstrap, etc)

### Condiciones para promoción

- Entregas en fecha
- Realizar frontend e integración con telegram.

## Entregas

Las entregas deberán realizarse el día pactado antes de las 19 Hs. con un tag en el repositorio llamado `Entrega_XX` correspondiente al número de entrega.

Las entregas se realizarán indicando el link al repositorio y el tag para la entrega.

Todo retraso en una entrega que no haya sido correctamente comunicado y justificado tendrá como penalización el agregado de nuevos requisitos para la aprobación final del TP.

### Entrega 1 - Esqueleto aplicación

Esqueleto de la aplicación WEB.

Se debe definir un primer approach hacia los recursos y URLs REST que se utilizarán para cumplir con las historias propuestas. Para esta entrega no es necesario implementar la persistencia, es suficiente con que el estado quede reflejado en memoria.

Para esta entrega sí es necesario que la app corra dentro de Docker.

### Entrega 2 - UI

Se debe poder interactuar con la app mediante la interfaz elegida.

### Entrega 3 - Persistencia con DB

Persistencia utilizando una base de datos. Se debe modificar la aplicación para que en vez de almacenar los datos en memoria, la misma lo haga utilizando alguna base a definir por el equipo.

> **Nota:** A fines pedagógicos se solicita que la base de datos sea NoSQL.

### Entrega 4 - Cloud

Para esta entrega la aplicación debe estar deployada en la nube, el deploy en la nube debe ser utilizando Docker containers.

### Entrega Final

Entrega final con detalles pulidos de funcionalidades faltantes y correcciones finales.

## Bonus

Integrar la aplicación con una fuente externa de información sobre figuritas del Mundial para autocompletar datos de jugadores, selecciones o categorías.
