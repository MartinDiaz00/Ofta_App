# Adecuación de OftaApp a las rúbricas EP4 (encargo) y EP5 (defensa)

**Fecha:** 2026-11-16  
**Fuentes revisadas:** `EA4_OftApp_Encargo__.pdf`, `EA5_OftApp_Defensa__.pdf`, `Rubrica.docx`, `docs/academico/Rubricas_Evaluaciones_2_y_3_DSY1105_VCM.pdf`.

> Este documento separa **evidencia ejecutada** de **requisitos aún pendientes**. No se afirma que esté todo completo: la rúbrica EP4/EP5 es más exigente que la EP2/EP3 y varios ítems requieren trabajo adicional y evidencia externa (GitHub/Trello/AVA).

## 1. Correcciones críticas detectadas en la verificación en dispositivo

Estas correcciones salieron de probar la app en el teléfono **KINGKONG 9** contra el microservicio HAPI FHIR real:

| Problema encontrado | Causa | Corrección |
|---|---|---|
| No se podía guardar **ningún examen**: "La fecha del examen no es válida." | `FormularioExamenViewModel` convertía la fecha a ISO y `ExamenRepository` la convertía una segunda vez | El ViewModel envía la fecha visible (`dd/MM/uuuu`) y el repositorio hace la única conversión. Test de contrato agregado. |
| Cada guardado creaba un `Patient`/`DiagnosticReport` nuevo en FHIR | Los IDs locales (`pac-…`, `exa-…`) se descartaban y siempre se hacía POST | Búsqueda por identificador estable (RUT / id de examen) antes de crear; si existe, se reutiliza con PUT. |
| Referencia `subject` rota en `DiagnosticReport` | Se enviaba el UUID local del paciente en vez del ID asignado por HAPI | Se resuelve/crea el `Patient` remoto y se usa su ID real en `Patient/{id}`. |
| PUT de examen devolvía HTTP 400 | El cuerpo del `DiagnosticReport` no incluía el `id` del recurso a actualizar | Se asigna el ID remoto al cuerpo antes del PUT (verificado 200 en dispositivo). |
| Fallos de red/FHIR invisibles para el usuario | El resultado de la sincronización best-effort se ignoraba | `SincronizacionMonitor`: snackbar + banner persistente de **"Sincronización pendiente"**. |
| `PdfReport` descartaba líneas que no cabían en la página | `return@forEach` silencioso al superar el alto | Paginación con encabezado/pie y número de página. |
| Transiciones inválidas se rechazaban tarde (error genérico) | El ViewModel no validaba `validarTransicion` | Validación en el formulario con error en el campo Estado. |
| Se podía agendar un examen en cualquier fecha | El formulario permitía elegir fecha con el calendario nativo | Fecha fija al día actual (campo no editable) y validación que exige "hoy" para exámenes nuevos. |
| El botón "Probar conexión API/HAPI FHIR" no explicaba su uso | Falta texto de ayuda | Se agregó descripción de qué consulta (/metadata) y qué muestra al hacer clic, incluido que no modifica datos. |

## 2.Estado frente a la rúbrica EP4 (encargo, 6%)

| Requisito EP4 | Estado | Evidencia / pendiente |
|---|---|---|
| 1. Interfaz accesible, login por rol, formularios, navegación, Material 3 | **Implementado** | Compose + Material 3, 4 roles, validaciones por campo. Falta evidencia de TalkBack/contraste/48 dp y recorrido accesible completo. |
| 2. Microservicios Spring Boot con BD, CRUD y Swagger/OpenAPI | **Parcial** | `ofta-fhir-service` con `Patient`/`DiagnosticReport` + `/api/status`. Se agregó `springdoc-openapi` (`/swagger-ui/index.html`, `/v3/api-docs`). **Persistencia aún en memoria** (requiere HAPI JPA + BD para nivel completo). |
| 3. Integración móvil + API externa (HAPI FHIR) | **Implementado y verificado** | Retrofit + coroutines + Repository; HAPI FHIR R4 real. Logcat en dispositivo: `GET ?identifier`, `POST 201`, `PUT 200`, conteos consultables desde Perfil. |
| 4. Persistencia offline robusta, "Modo offline"/"Sincronización pendiente" y reintento | **Parcial** | Room como fuente de verdad; banner de modo offline y estado pendiente agregados. **Falta reintento/manual sync y cifrado de token con Keystore**. |
| 5. Visor PDF integrado (PdfRenderer) + imágenes con Coil/zoom | **Pendiente** | Hoy se genera PDF y se abre/comparte con FileProvider (verificado en detalle). No hay visor embebido ni imágenes de retina con zoom. |
| 6. Cobertura >90% (ViewModel + Repository) con reporte, JUnit/MockK y Compose UI Test | **Parcial** | JUnit4 + MockWebServer + tests de reglas/mappers/remote. Se agregó `LoginScreenUiTest` (Compose UI Test). **Falta medir cobertura (Kover/JaCoCo) y llegar a >90%; MockK no está incorporado**. |
| 7. APK release firmada y versionada | **Implementado** | `signingConfigs` leyendo `keystore.properties` (ignorado por Git), `.jks` fuera del repo, `versionCode 2` / `versionName 1.1.0`. Ver `keystore.properties.example`. |
| 8. Colaboración GitHub/Trello | **Evidencia externa pendiente** | No se inventan commits ni tableros. |
| 9. Documentación profesional (README con integrantes, arquitectura, endpoints, Swagger, instalación) | **Parcial** | README y este documento; falta completar nombres/integrantes, URL Swagger y pasos de firma en el README principal. |
| 10. Evidencia verificable (capturas, Swagger, cobertura, offline/online, versión APK) | **Parcial** | Evidencia de E2E y HTTP en Logcat; faltan capturas limpias, captura de Swagger y reporte de cobertura. |

## 3. Estado frente a la rúbrica EP5 (defensa individual, 24%)

- Funcionamiento y arquitectura (IE 3.1.1/3.1.2): el recorrido login → listado → detalle → examen y el flujo UI→ViewModel→Repository→API/Room están implementados y fueron probados en dispositivo.
- Modificación en tiempo real (IE 3.1.3, 40%): requiere práctica individual; se recomienda practicar agregar un campo ficticio y propagarlo por Entity/DTO/endpoint/Repository/ViewModel/Compose.
- Pruebas (IE 3.2.1/3.2.2): hay tests JVM y un Compose UI Test; ejecutar en vivo `gradlew :app:testDebugUnitTest` y `connectedDebugAndroidTest`.
- Firma (IE 3.3.1): configuración lista; **no mostrar la clave privada ni contraseñas**.
- Colaboración (IE 3.3.2): evidencia externa pendiente.

## 4. Verificación ejecutada en esta sesión (evidencia real)

- `gradlew :app:testDebugUnitTest :app:assembleDebug -PFHIR_BASE_URL=http://127.0.0.1:8080/fhir/` → **BUILD SUCCESSFUL** (incluye nuevos tests de mappers, sincronización y reglas).
- `mvn test` en `ofta-fhir-service` → **7 tests, 0 fallos**.
- `node ofta-fhir-service/e2e-check.js` contra **HAPI FHIR real** → 11 comprobaciones OK (metadata, create/search/update Patient y DiagnosticReport, sin duplicados, referencia al Patient real).
- Dispositivo **KINGKONG 9**: app desinstalada y reinstalada; login `admin.ofta`, "Probar conexión API/HAPI FHIR" → **FHIR OK**; alta de paciente con RUT aleatorio `15.790.867-2` (validación visual + sync `PUT /Patient/1001`); alta de examen (`POST /DiagnosticReport` 201) y edición (`PUT /DiagnosticReport/2003` 200, total sin duplicados).
- `:app:connectedDebugAndroidTest` en KINGKONG 9 → **BUILD SUCCESSFUL**.

## 5. Prioridades para completar la rúbrica

1. **Persistencia remota real** (HAPI JPA + BD) para el indicador 2 de EP4.
2. **Visor PDF con PdfRenderer + imágenes sintéticas con Coil/Telephoto y zoom** (ítem 5, muy visible en la defensa).
3. **Cobertura >90% con reporte Kover/JaCoCo**, migrar tests a MockK y ampliar ViewModel/Repository.
4. **Cifrado de sesión** con EncryptedSharedPreferences/Keystore y reintento de sincronización.
5. **Accesibilidad**: etiquetas semánticas, TalkBack, contraste y controles de 48 dp con evidencia capturada.
6. **GitHub/Trello/AVA**: commits propios, tablero, capturas, Swagger y APK firmada; practicar la modificación en tiempo real.
