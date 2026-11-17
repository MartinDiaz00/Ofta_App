# Informe final — Tercera evaluación (Ofta_App / DSY1105)

**Fecha del informe:** 2026-05 (generado en sesión de trabajo sobre el repositorio).  
**Workspace:** `C:\Users\Martin\Desktop\APPM\Ofta_App-main`  
**Importante:** Este informe distingue entre cambios implementados en código/configuración y verificaciones efectivamente ejecutadas en este entorno. **No se inventan despliegues, ejecuciones de pruebas ni APKs generados.**

## 1. Diagnóstico inicial del proyecto

Antes de modificar código se realizó una **auditoría completa** (Fase 1). Resumen:
- El proyecto era un **MVP Android local** (Kotlin + Jetpack Compose + Material 3 + MVVM + Room/SQLite), con datos ficticios, autenticación demo local y sin conexión a Internet.
- **No existían** dependencias/configuración de Retrofit/OkHttp, ni integración FHIR/HAPI, ni permiso `INTERNET`, ni backend/microservicio, ni Docker.
- Existían pruebas unitarias de lógica local (`Validadores`, reglas de rol/examen, filtros). UI/navegación implementadas pero pendientes de verificación integral en dispositivo/emulador.
- Requisitos de tercera evaluación (Retrofit + HAPI FHIR, API real, backend/microservicio desplegable, testing de integración/API, APK verificable) estaban **faltantes o parciales**.

La tabla de clasificación detallada de la auditoría fue entregada en la conversación de Fase 1.

## 2. Tecnologías y herramientas identificadas

| Capa | Tecnologías |
|---|---|
| Android | Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel/StateFlow, Room+KSP, Coroutines, FileProvider, notificaciones locales |
| Red/API (agregado) | **Retrofit 2.11**, OkHttp 4.12 + logging interceptor, Gson converter, `BuildConfig.FHIR_BASE_URL` configurable |
| Interoperabilidad clínica | **FHIR R4** (modelos/JSON compatibles) consumidos desde Android; **HAPI FHIR 7.4.5** en backend (servidor RESTful R4) |
| Backend / microservicio (agregado) | Java 17, Spring Boot 3.3.5, HAPI FHIR (`hapi-fhir-base`, `hapi-fhir-server`, `hapi-fhir-structures-r4`), endpoint propio `/api/status` |
| Despliegue (configurado) | Dockerfile (multi-stage Maven) + `docker-compose.yml` |
| Testing | JUnit4 (Android local), kotlinx-coroutines-test, **MockWebServer** (Retrofit/FHIR HTTP), JUnit Jupiter (backend Spring Boot tests) |

## 3. Requisitos que ya estaban implementados (conservados)

- Estructura MVVM, Compose, navegación por rutas, pantallas principales y componentes.
- Persistencia Room (pacientes, usuarios, exámenes, documentos) y datos demo.
- Validaciones de dominio/formularios (RUT, fechas, reglas de estado, permisos por rol).
- Generación de PDF (`PdfReport`), adjuntos/documentos, notificaciones locales al validar/entregar.
- Pruebas unitarias locales existentes: `ValidadoresTest`, `ReglasExamenTest`, `ReglasRolTest`, `FiltrosExamenTest`.

**No se eliminaron funcionalidades existentes**; los cambios fueron aditivos/mínimos y orientados a compatibilidad.

## 4. Requisitos que estaban parcialmente implementados

- **Interfaces/navegación:** implementadas en código; se agregó mejora visual puntual (tema clínico más profesional manteniendo identidad verde) y estado visible de conexión FHIR en Perfil; verificación completa en dispositivo/emulador **pendiente** en este entorno.
- **Pruebas/testing:** existían pruebas locales; se agregaron pruebas de API/Retrofit (MockWebServer) y pruebas backend (HAPI/FHIR providers + status). Ejecución completa **no verificada** aquí por falta de SDK Android/Maven en entorno.

## 5. Requisitos que faltaban (implementados en código/config)

- Retrofit + configuración HTTP, modelos FHIR JSON, mappers dominio↔FHIR.
- Backend/microservicio HAPI FHIR R4 con recursos `Patient` y `DiagnosticReport` (create/read/update/search), validaciones básicas y manejo de errores FHIR.
- Configuración de despliegue reproducible (Docker/Compose).
- Permiso `INTERNET`, `usesCleartextTraffic` para entorno de desarrollo HTTP, URL FHIR configurable.
- Integración best-effort de sincronización FHIR desde repositorios (guardar paciente/examen, cambio de estado).
- Pruebas de comunicación/procesamiento de respuestas/errores HTTP-FHIR.

## 6. Mejoras y modificaciones realizadas

1. **Backend HAPI FHIR (nuevo microservicio):** servidor RESTful R4 en `/fhir/*` con `PatientProvider` y `DiagnosticReportProvider` (almacenamiento en memoria, thread-safe) + `GET /api/status`. Validaciones: `Patient.name` obligatorio; `DiagnosticReport.subject` obligatorio, defaults de `status/code` razonables para demo.
2. **Despliegue:** `ofta-fhir-service/Dockerfile` (build Maven + JRE 17) y `docker-compose.yml` en raíz.
3. **Android – Retrofit/FHIR:** `FhirApiService`, `FhirNetworkModule`, `FhirRemoteService`, DTOs FHIR (`FhirModels`), `FhirMappers` (Paciente↔Patient, Examen↔DiagnosticReport; mapeo de estados a `registered/final/corrected/amended`). BuildConfig `FHIR_BASE_URL` con override por `-PFHIR_BASE_URL` o env.
4. **Integración app:** `PacienteRepository` y `ExamenRepository` aceptan `FhirRemoteService` opcional y realizan sincronización **best-effort** tras guardar/cambiar estado (no bloquea el flujo local si el servidor no está disponible, preservando funcionalidad existente).
5. **UI/UX:** Perfil actualizado con texto preciso sobre API/FHIR y botón “Probar conexión API/HAPI FHIR” (muestra endpoint y resultado). Ajuste de tema (`Color.kt`) a paleta teal clínica más profesional manteniendo identidad verde.
6. **Aviso de conectividad:** se mantiene `AvisoSinConexion`/`ACCESS_NETWORK_STATE`; la app ya no es exclusivamente local cuando hay backend disponible (sincronización FHIR best-effort).

## 7. Archivos nuevos y archivos modificados

### Nuevos
- `Ofta_App-main/ofta-fhir-service/pom.xml`
- `Ofta_App-main/ofta-fhir-service/Dockerfile`
- `Ofta_App-main/ofta-fhir-service/README.md`
- `Ofta_App-main/ofta-fhir-service/src/main/resources/application.yml`
- `Ofta_App-main/ofta-fhir-service/src/main/java/com/equipo7/oftafhir/OftaFhirServiceApplication.java`
- `Ofta_App-main/ofta-fhir-service/src/main/java/com/equipo7/oftafhir/api/EstadoMicroservicioController.java`
- `Ofta_App-main/ofta-fhir-service/src/main/java/com/equipo7/oftafhir/fhir/OftaFhirServerConfig.java`
- `Ofta_App-main/ofta-fhir-service/src/main/java/com/equipo7/oftafhir/fhir/PatientProvider.java`
- `Ofta_App-main/ofta-fhir-service/src/main/java/com/equipo7/oftafhir/fhir/DiagnosticReportProvider.java`
- `Ofta_App-main/ofta-fhir-service/src/test/java/com/equipo7/oftafhir/EstadoMicroservicioControllerTest.java`
- `Ofta_App-main/ofta-fhir-service/src/test/java/com/equipo7/oftafhir/fhir/FhirProviderValidationTest.java`
- `Ofta_App-main/docker-compose.yml`
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/network/FhirNetworkModule.kt`
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/network/FhirRemoteService.kt`
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/network/api/FhirApiService.kt`
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/network/fhir/FhirModels.kt`
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/network/fhir/FhirMappers.kt`
- `OftaAppKotlin/app/src/test/java/com/equipo7/oftaapp/network/FhirApiServiceTest.kt`
- `Ofta_App-main/INFORME_FINAL_TERCERA_EVALUACION.md`

### Modificados
- `OftaAppKotlin/gradle/libs.versions.toml` (Retrofit/OkHttp/Gson/MockWebServer/coroutines-test)
- `OftaAppKotlin/app/build.gradle.kts` (deps, `buildConfig=true`, `FHIR_BASE_URL`)
- `OftaAppKotlin/app/src/main/AndroidManifest.xml` (`INTERNET`, `usesCleartextTraffic`)
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/OftaApplication.kt` (inyección `FhirRemoteService`)
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/repository/PacienteRepository.kt` (sync FHIR best-effort)
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/repository/ExamenRepository.kt` (sync FHIR best-effort)
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/ui/screens/PerfilScreen.kt` (estado/prueba FHIR, texto actualizado)
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/ui/navigation/OftaNavHost.kt` (pasa `fhirRemoteService`)
- `OftaAppKotlin/app/src/main/java/com/equipo7/oftaapp/ui/theme/Color.kt` (refinamiento clínico)

## 8. Explicación de la integración Retrofit y HAPI FHIR

- **Retrofit (Android, cliente HTTP):** `FhirApiService` define endpoints REST/FHIR (`GET/POST/PUT /Patient`, `/DiagnosticReport`, `/metadata`) con `suspend` + `Response<T>`. `FhirNetworkModule` configura `OkHttpClient` (timeouts, header `Accept: application/fhir+json`, logging en debug) y `Retrofit` con `GsonConverterFactory`. La URL base viene de `BuildConfig.FHIR_BASE_URL` (configurable por Gradle/env; por defecto emulador `10.0.2.2`). `FhirRemoteService` encapsula llamadas, procesa éxito/error HTTP y convierte respuestas.
- **HAPI FHIR (backend, estándar FHIR):** `ofta-fhir-service` registra un `RestfulServer` (HAPI) en `/fhir/*` con proveedores R4 para `Patient` y `DiagnosticReport`. Recibe/envía recursos FHIR JSON y aplica validaciones básicas; expone CapabilityStatement vía `/metadata`. Android envía estructuras compatibles FHIR R4 (resourceType, identifiers, references `Patient/{id}`, status, code, subject, effectiveDateTime, conclusion).
- **Separación de responsabilidades:** Retrofit = transporte HTTP y serialización en Android. HAPI FHIR = implementación del servidor/modelo FHIR en backend. Se evita duplicar lógica de servidor FHIR en el cliente.

## 9. Explicación del funcionamiento del microservicio

- **Nombre:** `ofta-fhir-service`
- **Responsabilidad:** proveer interoperabilidad FHIR R4 para datos clínicos demo (pacientes/exámenes oftalmológicos) y un endpoint de estado operativo.
- **Endpoints FHIR:** `/fhir/Patient`, `/fhir/DiagnosticReport` (y `/fhir/metadata`), operaciones create/read/update/search soportadas por proveedores.
- **Endpoint propio:** `GET /api/status` → estado `UP`, ruta FHIR, versión R4, timestamp.
- **Errores/validación:** HAPI lanza `InvalidRequestException`/`ResourceNotFoundException` (traducidos a respuestas FHIR apropiadas por el servidor); Android maneja códigos HTTP y cuerpos de error.
- **Persistencia:** en memoria (demo). Reinicio del servicio limpia datos. Documentado en `ofta-fhir-service/README.md`.

## 10. Información del despliegue

- **Reproducible (configurado):** desde `Ofta_App-main/`: `docker compose up --build` (levanta `ofta-fhir-service` en puerto `8080`).
- **Local sin Docker:** `cd ofta-fhir-service && mvn spring-boot:run` (requiere Java 17 + Maven).
- **APK/dispositivo externo:** configurar `FHIR_BASE_URL` a `http://<IP_SERVIDOR>:8080/fhir/` al compilar; **no usar `localhost`** en dispositivo físico.
- **Verificación en este entorno:** **no se ejecutó Docker/Maven/Spring Boot** aquí (herramientas no verificadas/disponibles para ejecución completa). El despliegue queda **configurado pero no demostrado como “exitoso”** en este informe.

## 11. Pruebas unitarias y resultados

| Prueba | Tipo | Resultado en este entorno | Evidencia |
|---|---|---|---|
| `FhirApiServiceTest` (Android, Retrofit+MockWebServer) | Unit/JVM: POST Patient, search Bundle DiagnosticReport, error 400 OperationOutcome, GET metadata | **NO EJECUTADO** (AGP requiere SDK Android; `SDK location not found`) | Archivo: `app/src/test/java/com/equipo7/oftaapp/network/FhirApiServiceTest.kt`. Intento: `gradlew :app:testDebugUnitTest` → fallo por `local.properties`/ANDROID_HOME ausente. |
| `ValidadoresTest`, `ReglasExamenTest`, `ReglasRolTest`, `FiltrosExamenTest` | Unit/JVM locales existentes | **NO EJECUTADO** (misma limitación SDK/AGP) | Archivos en `app/src/test/java/com/equipo7/oftaapp/`. |
| `EstadoMicroservicioControllerTest`, `FhirProviderValidationTest` | Unit backend (JUnit Jupiter/HAPI) | **EJECUTADO (2026-11-10) → BUILD SUCCESS, 5 tests, 0 failures** | Maven 3.9.9 instalado localmente; se corrigieron incompatibilidades de API HAPI 7.4.5 (`ReferenceParam`/`StringParam`/`TokenParam`) en `DiagnosticReportProvider` y `PatientProvider` para compilar y pasar pruebas. |
| `ExampleInstrumentedTest` | Instrumented (plantilla) | **NO EJECUTADO** | Requiere emulador/dispositivo + SDK. |

**Errores encontrados durante intentos de verificación (inicial):** falta de SDK Android (`local.properties`/ANDROID_HOME) impidió tareas de test/compilación Gradle Android; Maven no disponible impidió `mvn test`. **Correcciones efectuadas:** ninguna sobre SDK/Maven del sistema (dependen del entorno). Se documenta cómo ejecutar cuando el entorno esté preparado.

### Verificación adicional (sesión USB, 2026-11-10)
- **Dispositivo:** KINGKONG_9 (`KK9250321021092`) conectado por USB; `adb reverse tcp:8080 tcp:8080` configurado.
- **Backend (mock USB):** `node ofta-fhir-service/mock-fhir-test-server.js` en ejecución; `GET /api/status` → UP y `GET /fhir/metadata` → 200 verificados desde el PC.
- **APK:** recompilado e instalado en dispositivo. Se detectó que un APK reinstalado apuntaba a `http://10.0.2.2:8080/fhir/` (error de conexión en “Probar conexión” y endpoint mostrado en Perfil); se corrigió con `gradlew.bat :app:clean :app:assembleDebug -PFHIR_BASE_URL=http://127.0.0.1:8080/fhir/` y `adb install -r` → **Success**. APK final: `OftaAppKotlin/app/build/outputs/apk/debug/app-debug.apk`.
- **Tests automatizados:** `:app:testDebugUnitTest` → **BUILD SUCCESSFUL**; `:app:connectedDebugAndroidTest` en KINGKONG 9 → **BUILD SUCCESSFUL** (`ExampleInstrumentedTest`).
- **Pruebas en dispositivo (ADB/uiautomator, mock activo):** login con cuenta demo `admin.ofta` (pass `1234`) correcto; dashboard/Inicio con métricas y últimos exámenes visible; **Perfil → “Probar conexión API/HAPI FHIR”** → **“FHIR OK (http://127.0.0.1:8080/fhir/)”**; pestaña **Pacientes** carga listado/búsqueda (10 registrados) y acciones; pestaña **Exámenes** carga listado con filtros/estados; navegación a **detalle de examen** funcional (datos, estado, observaciones, adjunto PDF y acciones Ver/Compartir PDF). Se probó **editar/guardar paciente**: `POST http://127.0.0.1:8080/fhir/Patient` → **201 Created** observado en Logcat (sync FHIR best-effort contra mock). Sin `FATAL`/`AndroidRuntime` observados en logcat filtrado durante estas pruebas.
- **Backend (2026-11-10):** Maven 3.9.9 instalado en `C:\Users\Martin\Tools\apache-maven-3.9.9`; `mvn test` en `ofta-fhir-service` → **BUILD SUCCESS** (5 tests, 0 failures). Se corrigió compilación por API HAPI 7.4.5 en `fhir/DiagnosticReportProvider.java` y `fhir/PatientProvider.java`.
- **Pendiente:** E2E completo de creación/edición de pacientes y exámenes, cambios de estado con notificaciones y sincronización FHIR visible, pruebas por todos los roles, adjuntos/PDF en flujo real, y pruebas con **HAPI FHIR real** (`docker compose up --build` o `mvn spring-boot:run`) + evidencia de despliegue. Docker Desktop instalado vía winget pero puede requerir configuración/reinicio para usarse.

## 12. Verificación de interfaces y navegación

- **Código:** pantallas Compose y `OftaNavHost`/`Rutas` revisados; Perfil incluye prueba de conexión FHIR. Navegación/roles existentes preservados.
- **Ejecución manual en dispositivo/emulador:** **NO REALIZADA** en este entorno (sin SDK/emulador/dispositivo configurado). Requiere: abrir `OftaAppKotlin` en Android Studio, sincronizar Gradle, ejecutar app, validar flujos por rol, login, CRUD de exámenes/pacientes, adjuntos/PDF, notificaciones y prueba FHIR con backend levantado.
- **Responsive/profesionalización:** se aplicó refinamiento de tema y mejora funcional visible; **no** se realizó rediseño completo de todas las pantallas en esta sesión. Se recomienda iteración visual adicional con verificación en múltiples tamaños.

## 13. Ubicación del APK generado

- **APK generado en este entorno:** **SÍ (debug, verificado en sesión 2026-11-10).**
- **Ubicación:** `OftaAppKotlin/app/build/outputs/apk/debug/app-debug.apk` (~20 MB). Instalado en KINGKONG_9 vía `adb install -r` (**Success**).
- **Compilación recomendada (USB):** `cd OftaAppKotlin && gradlew.bat :app:assembleDebug -PFHIR_BASE_URL=http://127.0.0.1:8080/fhir/` (con `adb reverse tcp:8080 tcp:8080` activo). Para Wi-Fi usar la IP del PC en `FHIR_BASE_URL`. Release: `app/build/outputs/apk/release/` (requiere keystore/firmado; **no se inventan claves**).

## 14. Requisitos que quedaron pendientes / requieren acción externa

1. **Configurar Android SDK** (`ANDROID_HOME` o `local.properties` con `sdk.dir`) y ejecutar `testDebugUnitTest` / `assembleDebug`; registrar resultados reales.
2. **Instalar Maven/Docker** y ejecutar `mvn test` en `ofta-fhir-service` y/o `docker compose up --build`; verificar `/api/status` y `/fhir/metadata`.
3. **Verificación end-to-end:** app Android + backend FHIR corriendo (misma red/dispositivo), probar “Probar conexión API/HAPI FHIR”, crear/editar paciente/examen y confirmar recursos en servidor FHIR.
4. **APK instalable en dispositivo externo:** compilar con `-PFHIR_BASE_URL=http://<IP_BACKEND>:8080/fhir/`.
5. **UI/UX “nivel comercial” completo:** evolución adicional pantalla por pantalla (dashboard, listados, formularios, estados vacíos/carga/error, animaciones sutiles) con pruebas visuales; cambios realizados fueron conservadores para no romper funcionalidad.
6. **Persistencia real del microservicio:** migrar de almacenamiento en memoria a HAPI JPA server + BD si se requiere persistencia entre reinicios.
7. **Seguridad:** autenticación/autorización, HTTPS, gestión de secretos — fuera del alcance demo; no exponer credenciales.

## 15. Tabla final

| Requisito de evaluación | Estado final | Evidencia |
|---|---|---|
| Retrofit y HAPI FHIR | **IMPLEMENTADO en código / PARCIAL en verificación runtime** | Android: `FhirApiService`, `FhirNetworkModule`, `FhirRemoteService`, DTOs/mappers, deps Retrofit+OkHttp+Gson, `BuildConfig.FHIR_BASE_URL`. Backend: microservicio HAPI FHIR R4 (`Patient`, `DiagnosticReport`, `/fhir/*`, `/api/status`). Pruebas escritas; ejecución no verificada por entorno. |
| Funcionamiento de la app | **REQUIERE VERIFICACIÓN** (cambios aditivos, flujos locales preservados) | Integración best-effort FHIR en repositorios; botón de prueba FHIR en Perfil. Sin ejecución en dispositivo/emulador aquí. |
| Interfaces | **PARCIAL** | UI Compose existente + refinamiento de tema clínico + tarjeta/prueba FHIR en Perfil. Falta verificación visual completa y posible evolución adicional por pantalla. |
| Flujo de navegación | **REQUIERE VERIFICACIÓN** | `Rutas`/`OftaNavHost` sin cambios estructurales destructivos; flujo completo por rol no ejecutado en este entorno. |
| Backend y microservicio | **IMPLEMENTADO en código** | `ofta-fhir-service/` (Spring Boot + HAPI FHIR), endpoints FHIR + `/api/status`, README. |
| Despliegue | **CONFIGURADO / NO DEMOSTRADO** | `Dockerfile`, `docker-compose.yml`, instrucciones. No se ejecutó despliegue aquí. |
| Pruebas unitarias y testing | **PARCIAL** | Nuevas: `FhirApiServiceTest` (MockWebServer), tests backend HAPI/status. Existentes conservadas. **Ninguna ejecución registrada** por limitaciones de SDK/Maven. |
| APK Android Studio | **REQUIERE VERIFICACIÓN** | Manifest/deps/config actualizados; APK **no generado** (falta SDK). Ruta esperada: `app/build/outputs/apk/debug/app-debug.apk`. |

---
*Generado a partir de auditoría y modificaciones realizadas en el repositorio. Las afirmaciones de funcionamiento se limitan a lo verificable; los ítems marcados como “requiere verificación/no demostrado” necesitan ejecución en un entorno con Android SDK/Java-Maven/Docker y, para E2E, backend accesible.*