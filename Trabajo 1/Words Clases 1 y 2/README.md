# Ofta App

Aplicación móvil para registrar y consultar exámenes oftalmológicos usando datos ficticios. La idea es ordenar la información que actualmente puede estar repartida entre distintos archivos y correos.

## MVP
- Inicio de sesión con usuarios y roles ficticios.
- Registro y consulta de exámenes.
- Documentos de resultados simulados.
- Historial de exámenes.
- Búsqueda y filtros básicos.
- Estado y resumen del resultado.

## Identidad visual
- Principal: `#0B7A63`
- Secundario: `#4DB6AC`
- Fondo: `#F4FAF8`
- Texto: `#212121`
- Blanco: `#FFFFFF`

![Logo de Ofta App](docs/diseno/logo.png)

## Flujo principal
![Diagrama de Actividad UML](docs/diseno/flujo-usuario-uml.png)

```mermaid
flowchart TD
    A((Inicio)) --> B[Ingresar usuario y contraseña]
    B --> C{¿Credenciales válidas?}
    C -- No --> D[Mostrar error]
    D --> B
    C -- Sí --> E[Validar rol]
    E --> F[Mostrar menú principal]
    F --> G{Seleccionar opción}
    G -- Ver exámenes --> H[Ver listado de exámenes]
    G -- Registrar --> I[Registrar examen]
    G -- Historial --> J[Ver historial]
    G -- Buscar --> K[Buscar o filtrar]
    I --> L[Completar datos y adjuntar documento]
    L --> M{¿Campos completos?}
    M -- No --> L
    M -- Sí --> N[Guardar examen]
    H --> O[Seleccionar examen]
    J --> O
    K --> O
    N --> O
    O --> P[Ver detalle del examen]
    P --> Q{¿Tiene documento?}
    Q -- Sí --> R[Mostrar resultado o documento]
    Q -- No --> S[Mostrar datos disponibles]
    R --> T((Fin))
    S --> T
```

## Interfaces
Las imágenes están en `docs/diseno/interfaces/`.

- `login.png`
- `menu-principal.png`
- `registrar-examen.png`
- `listado-busqueda.png`
- `historial.png`
- `detalle-examen.png`

## Material Design 3
Se consideran principalmente `TopAppBar`, `NavigationBar`, `Card`, `Button`, `TextField` y `FloatingActionButton`, según la pantalla.

## Integrantes
- Diego Andres Diaz Hernandez — Backend y desarrollo
- Martin Andres Diaz Gonzalez — Líder y frontend

## Evidencias
- Clase 1: `docs/evidencias/clase-01/Evidencia_Clase_01_MVP_Equipo07.docx`
- Clase 2: `docs/evidencias/clase-02/Evidencia_Clase_02_Diseno_Equipo07.docx`
