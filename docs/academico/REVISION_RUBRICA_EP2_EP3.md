# Revisión de la rúbrica DSY1105 — Ofta_App

**Proyecto:** Ofta_App — registro y consulta de exámenes oftalmológicos.  
**Propósito:** contrastar el código del proyecto con la rúbrica de Evaluación Parcial 2 (EP2) y la defensa individual de Evaluación Parcial 3 (EP3).

> Importante: esta revisión distingue entre funciones presentes en el código y evidencias que todavía deben probarse en Android Studio o acreditarse en GitHub/Trello. No equivale a una certificación de compilación ni a una nota garantizada.

## 1. EP2 — Entrega grupal (8% de la asignatura)

| Indicador y ponderación | Estado en el proyecto | Qué hay y qué falta comprobar |
|---|---|---|
| **IE 2.1.1 — Interfaz y navegación (15%)** | **Implementado; probar recorrido completo** | Hay pantallas Compose con Material 3, menú lateral, inicio, listado, historial, formulario y detalle. Probar el recorrido desde inicio a listado, abrir un examen y volver atrás, y recorrer las opciones visibles según el rol. |
| **IE 2.1.2 — Formularios y validación visual (15%)** | **Implementado; probar casos válidos e inválidos** | El formulario exige paciente, fecha, sucursal, profesional, tipo, ojo y estado; presenta alertas por campos vacíos y fecha inválida. Se mantiene la fecha editable y se añade selector de fecha nativo. Probar un envío incompleto, una fecha imposible y un registro válido. |
| **IE 2.2.1 — Estado, ViewModel y lógica (10%)** | **Implementado; mejorable** | `ExamenViewModel`, `MenuViewModel` y `SesionViewModel` gestionan estados con `StateFlow`; la UI observa esos estados. La conexión a repositorios se instancia directamente desde los ViewModels, aceptable para el MVP de curso pero mejorable con inyección de dependencias. |
| **IE 2.2.2 — Listas, búsqueda, filtros y detalle (10%)** | **Implementado y reforzado** | El listado busca por paciente, tipo, estado, fecha y sucursal, abre el detalle y ahora incluye filtros visibles `Todos`, `Pendientes` y `Completados`. Probar combinaciones de búsqueda y estado, incluido un resultado vacío. |
| **IE 2.3.1 — MVVM y persistencia Room (15%)** | **Integración en código; requiere prueba en dispositivo** | Existen `AppDatabase`, entidades Room, DAO y repositorio; el registro de exámenes se guarda en SQLite local. La evidencia definitiva es crear un examen, cerrar por completo la aplicación, volver a abrirla y comprobar que sigue en el listado. No se debe afirmar que la prueba está aprobada hasta realizarla en el dispositivo. |
| **IE 2.3.2 — GitHub, documentación y planificación (20%)** | **Parcial / evidencia externa pendiente** | El ZIP incluye README, documentación académica y una revisión de rúbrica. El ZIP por sí solo no demuestra commits de cada integrante ni tareas en Trello. Revisar el repositorio real, agregar commits técnicos propios de cada integrante y mantener un tablero con tareas, responsables, estado y evidencia. No inventar commits ni capturas. |
| **IE 2.4.1 — Dos recursos del dispositivo (15%)** | **Hay tres recursos previstos; probarlos** | Selector de documentos PDF/imagen con `OpenDocument`, calendario nativo Android para elegir fecha y notificaciones locales con permiso de Android 13+. Probar cada uno en el teléfono y confirmar que el resultado se refleja en la aplicación. |

### Pruebas mínimas de aceptación para EP2

- [ ] La app abre en el teléfono conectado por depuración inalámbrica.
- [ ] Inicio de sesión con una cuenta de demostración y acceso correcto según rol.
- [ ] Navegar desde inicio al listado y abrir un detalle.
- [ ] Buscar por nombre de paciente y por tipo de examen.
- [ ] Filtrar `Pendientes` y `Completados`; luego volver a `Todos`.
- [ ] Intentar guardar con campos vacíos y comprobar el mensaje.
- [ ] Intentar guardar una fecha inválida escrita manualmente y comprobar el mensaje.
- [ ] Registrar un examen válido y comprobar el mensaje de confirmación.
- [ ] Cerrar la aplicación completamente, abrirla y verificar que el examen sigue guardado.
- [ ] Adjuntar un PDF o imagen y abrirlo desde el detalle.
- [ ] Elegir una fecha con el calendario nativo.
- [ ] Crear un recordatorio y aceptar el permiso de notificaciones cuando Android lo solicite.
- [ ] Revisar GitHub, commits de los integrantes y planificación en Trello o herramienta equivalente.

## 2. EP3 — Defensa individual (32% de la asignatura)

EP3 no se aprueba solamente agregando código: cada integrante debe poder ejecutar, explicar y modificar una parte real del proyecto sin apoyo de IA durante la defensa.

| Indicador y ponderación | Cómo demostrarlo individualmente |
|---|---|
| **IE 2.1.1 — Explica y modifica validaciones (20%)** | Explicar qué campos son obligatorios, mostrar un error por campos vacíos y una fecha imposible, y modificar una regla pequeña en vivo. |
| **IE 2.1.2 — Domina UI y navegación (7%)** | Recorrer inicio → exámenes → detalle → volver; explicar Material 3 y por qué la aplicación ayuda a consultar exámenes oftalmológicos. |
| **IE 2.2.1 — Modifica un componente o función (22%)** | Practicar cambiar un texto, agregar una opción de filtro o ajustar una validación, manteniendo la compilación. Entender el cambio, no memorizarlo solamente. |
| **IE 2.2.2 — Explica StateFlow, listas y filtros (7%)** | Mostrar `ExamenViewModel.kt`, explicar `ExamenUiState`, `StateFlow`, el filtro de texto y el filtro por estado, y relacionarlos con los chips del listado. |
| **IE 2.3.1 — Demuestra Room (18%)** | Crear un registro, consultarlo en la lista, cerrar y reabrir la app, y explicar qué hacen una Entity, un DAO, `AppDatabase` y `ExamenRepository`. |
| **IE 2.3.2 — Explica MVVM y colaboración (7%)** | Identificar la UI en `ui/screens`, los modelos en `model`, la lógica de presentación en `viewmodel` y acceso a datos en `repository`/`data/local`; mostrar los commits propios y las tareas asignadas. |
| **IE 2.4.1 — Explica recursos nativos y seguridad (7%)** | Explicar el selector de documentos, el calendario nativo, el permiso de notificaciones y por qué la app solo usa datos ficticios para la demostración. |
| **IE 2.4.2 — Ejecuta recursos nativos en la UI (12%)** | Demostrar en el teléfono la selección de documento, selección de fecha y notificación local; mostrar el resultado en pantalla. |

### Orden sugerido para una defensa de 15 minutos

1. **2 min:** explicar la necesidad que aborda Ofta_App y ejecutar la app.
2. **3 min:** recorrer pantallas y demostrar una validación fallida y un registro correcto.
3. **2 min:** mostrar búsqueda y filtros; explicar cómo `StateFlow` actualiza la UI.
4. **3 min:** demostrar persistencia Room y explicar Entity, DAO, Database y Repository.
5. **2 min:** demostrar selector de documento, calendario y notificación.
6. **3 min:** realizar una modificación pequeña previamente practicada y explicar por qué funciona.

## 3. Pendientes externos que el código no puede resolver por sí solo

1. Confirmar que todos los integrantes tienen participación verificable en GitHub mediante commits reales.
2. Mantener Trello u otra herramienta con tareas, responsable, estado y fecha.
3. Ejecutar en el teléfono todas las pruebas de aceptación de EP2 y guardar capturas propias como evidencia.
4. Cada integrante debe practicar individualmente la explicación y una modificación sencilla para EP3.
5. Antes de entregar, hacer Sync/Build en Android Studio y corregir cualquier error que aparezca en el equipo de desarrollo.

## 4. Cuentas de demostración

La aplicación usa cuentas locales de prueba con contraseña `1234`:

- `dra.fernandez` — Oftalmólogo
- `dr.silva` — Oftalmólogo
- `tens.martinez` — TENS
- `paciente.demo` — Paciente

Son credenciales de demostración, no deben usarse como cuentas reales.
