# Ofta_App — MVP académico de gestión de exámenes oftalmológicos

Aplicación Android desarrollada para la asignatura **DSY1105 — Desarrollo de Aplicaciones Móviles**. Su objetivo es centralizar el registro y la consulta de exámenes oftalmológicos de demostración.

> **Aviso:** todos los datos incluidos son ficticios. Este MVP es académico; no realiza diagnósticos y no reemplaza la evaluación ni el criterio de un profesional de salud.

## Funcionalidades

- Inicio de sesión con cuentas de demostración por rol, validación por campo y mostrar/ocultar contraseña.
- Resumen de exámenes y pacientes de ejemplo.
- Listado de exámenes con búsqueda por paciente, tipo, estado, fecha o sucursal.
- Registro y edición de exámenes con validación de campos, fecha y RUT formateado mientras se escribe.
- Selección de profesional desde los usuarios con rol Oftalmólogo registrados en Room.
- Detalle del examen, documentos adjuntos e informe PDF.
- Selección de fecha mediante calendario nativo de Android.
- Filtros visibles por estado: todos, pendientes y completados.
- Ficha de paciente con filtro por tipo de examen.
- Notificación local automática cuando un examen queda validado o entregado (Android puede solicitar permiso).
- Roles: TENS registra y corrige exámenes observados; Oftalmólogo valida, entrega y también puede crear pacientes; Administrador gestiona usuarios.
- Persistencia local con Room/SQLite; la aplicación no necesita conexión a Internet ni credenciales externas.

## Tecnologías

- Kotlin y Jetpack Compose.
- Material 3.
- MVVM con ViewModel y StateFlow.
- Room para almacenamiento local.
- FileProvider para abrir el PDF generado.
- Notificaciones locales de Android.

## Revisión de la rúbrica

Se incluye una revisión paso a paso de los indicadores EP2 y EP3 en `docs/academico/REVISION_RUBRICA_EP2_EP3.md`. Incluye las funciones presentes, las pruebas unitarias ejecutadas, las pruebas manuales pendientes y la evidencia externa que debe mantenerse en GitHub/Trello.

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
| Administrador | `admin.ofta` |
| Oftalmólogo | `dra.fernandez` |
| Oftalmólogo | `dr.silva` |
| TENS | `tens.martinez` |
| TENS | `tens.rojas` |
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

Se agregaron pruebas unitarias para validaciones, reglas de estado y permisos por rol en `app/src/test/java/com/equipo7/oftaapp/` (`ValidadoresTest`, `ReglasExamenTest`, `ReglasRolTest`, `FiltrosExamenTest`). Desde la carpeta `OftaAppKotlin` se ejecutan con `gradlew.bat testDebugUnitTest --no-daemon`; la compilación se verifica con `gradlew.bat assembleDebug --no-daemon`.

## Limitaciones conocidas

- La autenticación utiliza cuentas de demostración locales; no es un sistema de autenticación de producción.
- Los registros de ejemplo son ficticios y están destinados a pruebas académicas.
- El proyecto compila y las pruebas unitarias pasan; la validación manual en emulador o dispositivo queda pendiente de realizarse en un entorno con `adb` disponible.
