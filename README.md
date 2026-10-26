# Ofta_App — MVP académico de gestión de exámenes oftalmológicos

Aplicación Android desarrollada para la asignatura **DSY1105 — Desarrollo de Aplicaciones Móviles**. Su objetivo es centralizar el registro y la consulta de exámenes oftalmológicos de demostración.

> **Aviso:** todos los datos incluidos son ficticios. Este MVP es académico; no realiza diagnósticos y no reemplaza la evaluación ni el criterio de un profesional de salud.

## Funcionalidades

- Inicio de sesión con cuentas de demostración por rol.
- Resumen de exámenes y pacientes de ejemplo.
- Listado de exámenes con búsqueda por paciente, tipo, estado, fecha o sucursal.
- Registro de exámenes con validación de campos y fecha.
- Detalle del examen e informe PDF.
- Selección de fecha mediante calendario nativo de Android.
- Filtros visibles por estado: todos, pendientes y completados.
- Creación de recordatorio mediante notificación local (Android puede solicitar permiso).
- Persistencia local con Room/SQLite; la aplicación no necesita conexión a Internet ni credenciales externas.

## Tecnologías

- Kotlin y Jetpack Compose.
- Material 3.
- MVVM con ViewModel y StateFlow.
- Room para almacenamiento local.
- FileProvider para abrir el PDF generado.
- Notificaciones locales de Android.

## Revisión de la rúbrica

Se incluye una revisión paso a paso de los indicadores EP2 y EP3 en `docs/academico/REVISION_RUBRICA_EP2_EP3.md`. Incluye las funciones presentes, las pruebas que faltan por ejecutar y la evidencia externa que debe mantenerse en GitHub/Trello.

## Abrir el proyecto

1. Instala Android Studio con un Android SDK compatible con el `compileSdk` configurado en `OftaAppKotlin/app/build.gradle.kts`.
2. Descomprime el repositorio y abre la carpeta `OftaAppKotlin` en Android Studio.
3. Espera a que termine la sincronización de Gradle. Se necesita conexión la primera vez para descargar las dependencias.
4. Selecciona un emulador o conecta un dispositivo Android con depuración USB.
5. Ejecuta la configuración `app`.

También puedes ejecutar `gradlew.bat assembleDebug` desde PowerShell dentro de `OftaAppKotlin` después de que Gradle haya descargado las dependencias.

## Cuentas de demostración

Todas las cuentas utilizan la contraseña de demostración `1234`:

| Rol | Usuario |
|---|---|
| Oftalmólogo | `dra.fernandez` |
| Oftalmólogo | `dr.silva` |
| TENS | `tens.martinez` |
| Paciente | `paciente.demo` |

Estas cuentas son solo para probar el MVP. No reutilices contraseñas reales.

## Estructura principal

- `app/src/main/java/com/equipo7/oftaapp/ui`: pantallas y componentes Compose.
- `model`: modelos del dominio.
- `viewmodel`: estado de pantalla y lógica de presentación.
- `repository`: acceso a los datos de la aplicación.
- `data/local`: entidades, DAO y base de datos Room.
- `notifications`: notificaciones locales.
- `util/PdfReport.kt`: generación del informe PDF.

## Pruebas

Se agregaron pruebas unitarias para las validaciones de fecha, correo y RUT en `app/src/test/java/com/equipo7/oftaapp/ValidadoresTest.kt`. En Android Studio se pueden ejecutar desde la carpeta `OftaAppKotlin` con `gradlew.bat test`.

## Limitaciones conocidas

- La autenticación utiliza cuentas de demostración locales; no es un sistema de autenticación de producción.
- Los registros de ejemplo son ficticios y están destinados a pruebas académicas.
- Antes de una entrega, sincroniza Gradle y ejecuta las pruebas en Android Studio. El proyecto modificado no se ha podido compilar en este entorno.
