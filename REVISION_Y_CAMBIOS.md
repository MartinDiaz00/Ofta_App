# Revisión y cambios de Ofta_App

## Cambios realizados en esta copia

- Se eliminó el acceso JDBC directo a PostgreSQL/Neon y el JAR de PostgreSQL. El MVP ya no contiene credenciales de conexión ni necesita permiso de Internet para cargar sus datos.
- Se incorporó Room con cuatro entidades: pacientes, usuarios, exámenes y documentos adjuntos; además se añadieron DAO y la clase `AppDatabase`.
- Se cambió el repositorio de exámenes para leer y guardar datos localmente y se añadieron datos ficticios de demostración.
- Se eliminó `runBlocking` de los flujos de consulta/registro del repositorio y se añadieron estados de carga y error en los ViewModels.
- El formulario ahora muestra el éxito solo después de que se confirma el guardado, valida la fecha estrictamente, permite escribirla manualmente o elegirla con el calendario nativo de Android, y adjuntar PDF o imágenes mediante el selector de documentos.
- Se agregó la apertura del documento adjunto desde el detalle, cuando existe un URI guardado.
- Se agregó la solicitud del permiso de notificaciones en Android 13 o superior y una notificación automática cuando un examen cambia a Validado o Entregado. Se quitó el botón de recordatorio manual y la acción de correo de prueba.
- Se añadió un aviso de MVP académico con datos ficticios en el login y en los PDF.
- Se ajustaron los permisos visibles para roles y el listado del rol Paciente filtra por su ficha de demostración.
- El login valida usuario y contraseña por campo, muestra errores con `OutlinedTextField` y permite mostrar/ocultar la contraseña.
- El formulario toma los profesionales desde los usuarios Oftalmólogo de Room, limita las observaciones a 500 caracteres con contador y formatea el RUT mientras se escribe.
- La ficha de paciente permite filtrar sus exámenes por tipo y el detalle bloquea la eliminación de documentos cuando el examen está Entregado.
- TENS puede corregir exámenes Observados y devolverlos a Pendiente; Oftalmólogo puede crear pacientes; TENS y Administrador pueden eliminar documentos.
- Se añadieron validadores reutilizables y pruebas unitarias para fecha, correo, RUT, observaciones, reglas de estado y permisos por rol. `gradlew.bat testDebugUnitTest` y `gradlew.bat assembleDebug` se ejecutaron correctamente.
- Se actualizó el README, se ordenaron copias de las evidencias y documentos en `docs/`, y se añadió `docs/academico/REVISION_RUBRICA_EP2_EP3.md` con el análisis indicador por indicador y una lista de pruebas para EP2/EP3.
- Se añadió `android.disallowKotlinSourceSets=false` a `gradle.properties` para conservar la solución de compatibilidad que permitió al usuario sincronizar el proyecto en su versión de Android Studio.

## Limitaciones importantes

- **Compilación y pruebas unitarias verificadas en este entorno; falta la prueba manual en dispositivo.** No hay `adb` ni emulador disponible, por lo que no se pudo ejecutar la app ni probar permisos, notificaciones y persistencia en un equipo real.
- La autenticación de demostración consulta la tabla Room de usuarios y usa hash con salt; las cuentas siguen siendo ficticias y no deben usarse en producción.
- La navegación usa Navigation Compose en `ui/navigation/OftaNavHost.kt`.
- Los documentos seleccionados se copian a `filesDir/documentos` con límite de 10 MB; el PDF del informe se guarda en `filesDir/reportes`.
- El listado permite combinar búsqueda de texto con filtros visibles por estado (Todos, Pendientes y Completados).
- La validación manual de las pruebas de aceptación de EP2 sigue pendiente y debe hacerse antes de la entrega.
- No se ha creado un tablero Trello ni se han fabricado commits o evidencias de trabajo colaborativo.

## Pasos obligatorios de comprobación

1. Si alguna vez existió una contraseña de Neon activa y publicada, cambiarla; la eliminación del código no invalida una contraseña expuesta en el historial remoto.
2. Abrir `OftaAppKotlin` en Android Studio y sincronizar Gradle con Internet disponible.
3. Ejecutar `gradlew.bat testDebugUnitTest --no-daemon` y `gradlew.bat assembleDebug --no-daemon` (ya verificados en este entorno).
4. Probar en un emulador o dispositivo: login por rol, registro/edición, búsqueda, filtros, reinicio de la aplicación, adjuntos, eliminación de documentos y permiso de notificaciones.
5. Corregir cualquier error de ejecución antes de subir cambios a GitHub y mantener las evidencias reales de commits y Trello.
