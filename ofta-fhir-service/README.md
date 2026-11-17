# ofta-fhir-service (Microservicio HAPI FHIR R4)

Microservicio backend para **Ofta_App** (DSY1105, tercera evaluación). Proporciona una implementación **FHIR R4** mediante **HAPI FHIR** (servidor RESTful) y un endpoint propio de estado.

## Responsabilidad
- Exponer API FHIR en `/fhir/*` para recursos clínicos usados por la app Android (vía Retrofit).
- Recursos soportados (demo): `Patient`, `DiagnosticReport` (operaciones básicas: create/read/update/search).
- Endpoint operativo: `GET /api/status`.

## Requisitos
- Java 17+
- Maven 3.9+ (o Docker)

## Ejecución local (Maven)
```bash
cd ofta-fhir-service
mvn spring-boot:run
```
Servicio: `http://localhost:8080`  
FHIR base: `http://localhost:8080/fhir`  
Estado: `http://localhost:8080/api/status`  
Metadata: `http://localhost:8080/fhir/metadata`

## Despliegue reproducible (Docker Compose)
Desde la raíz `Ofta_App-main/`:
```bash
docker compose up --build
```

## Configuración Android
La app usa `BuildConfig.FHIR_BASE_URL` (por defecto `http://10.0.2.2:8080/fhir/` para emulador Android). Para dispositivo físico, configurar la IP/host del backend:
```bash
./gradlew :app:assembleDebug -PFHIR_BASE_URL=http://<IP_BACKEND>:8080/fhir/
```
No usar `localhost` en un APK instalado en un dispositivo externo.

## Pruebas
```bash
mvn test
```
Incluye pruebas unitarias de validación de proveedores FHIR y del endpoint de estado.

## Limitaciones (demo académico)
- Almacenamiento **en memoria** en los proveedores FHIR: los datos se reinician al reiniciar el servicio (adecuado para demostración; para persistencia real usar HAPI JPA server + BD).
- Seguridad/autenticación no productiva (entorno académico). No incluir credenciales reales.
