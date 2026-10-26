# Revisión y cambios de Ofta_App

## Cambios realizados en esta copia

- Se eliminó el acceso JDBC directo a PostgreSQL/Neon y el JAR de PostgreSQL. El MVP ya no contiene credenciales de conexión ni necesita permiso de Internet para cargar sus datos.
- Se incorporó Room con cuatro entidades: pacientes, usuarios, exámenes y documentos adjuntos; además se añadieron DAO y la clase `AppDatabase`.
- Se cambió el repositorio de exámenes para leer y guardar datos localmente y se añadieron datos ficticios de demostración.
- Se eliminó `runBlocking` de los flujos de consulta/registro del repositorio y se añadieron estados de carga y error en los ViewModels.
- El formulario ahora muestra el éxito solo después de que se confirma el guardado, valida la fecha estrictamente, permite escribirla manualmente o elegirla con el calendario nativo de Android, y adjuntar PDF o imágenes mediante el selector de documentos.
- Se agregó la apertura del documento adjunto desde el detalle, cuando existe un URI guardado.
- Se agregó la solicitud del permiso de notificaciones en Android 13 o superior y un recordatorio asociado al examen. Se quitó la acción de correo de prueba.
- Se añadió un aviso de MVP académico con datos ficticios en el login y en los PDF.
- Se ajustaron los permisos visibles para roles y el listado del rol Paciente filtra por su ficha de demostración.
- Se añadieron validadores reutilizables y pruebas unitarias para fecha, correo y RUT. La ejecución de estas pruebas queda pendiente de hacer en Android Studio.
- Se actualizó el README, se ordenaron copias de las evidencias y documentos en `docs/`, y se añadió `docs/academico/REVISION_RUBRICA_EP2_EP3.md` con el análisis indicador por indicador y una lista de pruebas para EP2/EP3.
- Se añadió `android.disallowKotlinSourceSets=false` a `gradle.properties` para conservar la solución de compatibilidad que permitió al usuario sincronizar el proyecto en su versión de Android Studio.

## Limitaciones importantes

- **No se pudo compilar ni ejecutar en este entorno.** El wrapper necesita descargar Gradle desde Internet, pero la conexión está bloqueada. Los cambios requieren sincronización y compilación en Android Studio antes de considerarlos verificados.
- La autenticación de demostración todavía se realiza con una lista local en `UsuarioRepository`; aunque existe la entidad Room para usuarios y se crean registros de demostración con hash, el login aún no consulta esa tabla. No usar este mecanismo en producción.
- La navegación continúa basada en el estado Compose existente; no se migró a Navigation Compose.
- La persistencia del documento guarda la URI otorgada por el selector, no copia el archivo a un directorio interno de la aplicación. Si el proveedor de archivos revoca el acceso, el adjunto podría dejar de abrirse.
- El listado permite combinar búsqueda de texto con filtros visibles por estado (Todos, Pendientes y Completados).
- Se necesita comprobar en Android Studio la compatibilidad exacta de las versiones de Gradle, Android Gradle Plugin, Kotlin, KSP y Room.
- No se ha creado un tablero Trello ni se han fabricado commits o evidencias de trabajo colaborativo.

## Pasos obligatorios de comprobación

1. Cambiar la contraseña de Neon si alguna vez estuvo activa y publicada; la eliminación del código no invalida una contraseña expuesta en el historial remoto.
2. Abrir `OftaAppKotlin` en Android Studio y sincronizar Gradle con Internet disponible.
3. Ejecutar `gradlew.bat test` y `gradlew.bat assembleDebug`.
4. Probar registro, búsqueda, reinicio de la aplicación, adjuntos y permisos de notificación en un emulador o dispositivo.
5. Corregir cualquier error de compilación o ejecución antes de subir cambios a GitHub.
