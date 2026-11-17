-- ============================================================
-- OftaApp - esquema completo para PostgreSQL / Neon
-- EJECUTAR en el SQL Editor de Neon (database = neondb).
-- ADVERTENCIA: DROP borra los datos actuales de estas tablas.
-- ============================================================

DROP VIEW IF EXISTS vw_historial_paciente;
DROP TABLE IF EXISTS auditoria;
DROP TABLE IF EXISTS notificaciones;
DROP TABLE IF EXISTS documentos_examen;
DROP TABLE IF EXISTS citas;
DROP TABLE IF EXISTS examenes;
DROP TABLE IF EXISTS usuario_roles;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS pacientes;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS fhir_resources;

CREATE TABLE roles (
    id          TEXT PRIMARY KEY,
    nombre      VARCHAR(40) NOT NULL UNIQUE,
    descripcion TEXT
);

CREATE TABLE pacientes (
    id                TEXT PRIMARY KEY,
    rut               VARCHAR(20) NOT NULL UNIQUE,
    nombre_completo   VARCHAR(120) NOT NULL,
    fecha_nacimiento  DATE,
    edad              INTEGER,
    prevision         VARCHAR(60) NOT NULL DEFAULT 'Sin especificar',
    correo            VARCHAR(120),
    telefono_principal VARCHAR(30),
    telefono_secundario VARCHAR(30),
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE usuarios (
    id             TEXT PRIMARY KEY,
    username       VARCHAR(60) NOT NULL UNIQUE,
    password_hash  TEXT NOT NULL,
    password_salt  TEXT NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    correo         VARCHAR(120),
    paciente_id    TEXT REFERENCES pacientes(id) ON DELETE SET NULL,
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE examenes (
    id             TEXT PRIMARY KEY,
    paciente_id    TEXT NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    identificador  VARCHAR(120) UNIQUE,          -- id local del examen (sync idempotente)
    tipo_examen    VARCHAR(120) NOT NULL,
    fecha          DATE NOT NULL,
    hora           VARCHAR(5) NOT NULL,
    sucursal       VARCHAR(120) NOT NULL,
    profesional    VARCHAR(120) NOT NULL,
    ojo_evaluado   VARCHAR(40) NOT NULL,
    estado         VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones  TEXT,
    creado_por_id  TEXT,
    created_at     BIGINT NOT NULL,
    updated_at     BIGINT NOT NULL
);
CREATE INDEX idx_examenes_paciente ON examenes(paciente_id);
CREATE INDEX idx_examenes_estado ON examenes(estado);
CREATE INDEX idx_examenes_fecha ON examenes(fecha);

CREATE TABLE documentos_examen (
    id             TEXT PRIMARY KEY,
    examen_id      TEXT NOT NULL REFERENCES examenes(id) ON DELETE CASCADE,
    nombre_original VARCHAR(200) NOT NULL,
    ruta_interna   TEXT NOT NULL,
    mime_type      VARCHAR(100),
    tamano_bytes   BIGINT NOT NULL DEFAULT 0,
    created_at     BIGINT NOT NULL
);
CREATE INDEX idx_documentos_examen ON documentos_examen(examen_id);

CREATE TABLE citas (
    id            TEXT PRIMARY KEY,
    paciente_id   TEXT NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    fecha         DATE NOT NULL,
    hora          VARCHAR(5) NOT NULL,
    tipo_examen   VARCHAR(120),
    sucursal      VARCHAR(120),
    profesional   VARCHAR(120),
    estado        VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_citas_paciente ON citas(paciente_id);

CREATE TABLE notificaciones (
    id          TEXT PRIMARY KEY,
    examen_id   TEXT REFERENCES examenes(id) ON DELETE CASCADE,
    titulo      VARCHAR(160) NOT NULL,
    mensaje     TEXT NOT NULL,
    leida       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE auditoria (
    id          BIGSERIAL PRIMARY KEY,
    tabla       VARCHAR(60) NOT NULL,
    operacion   VARCHAR(20) NOT NULL,
    registro_id TEXT NOT NULL,
    detalle     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE usuario_roles (
    usuario_id TEXT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_id     TEXT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

-- Tabla usada por el microservicio HAPI FHIR para persistir los recursos
-- Patient y DiagnosticReport que llegan desde la app Android (Retrofit).
CREATE TABLE fhir_resources (
    resource_type VARCHAR(40) NOT NULL,
    resource_id   VARCHAR(120) NOT NULL,
    identifier_system VARCHAR(200),
    identifier_value  VARCHAR(200),
    subject_reference VARCHAR(200),
    status        VARCHAR(40),
    payload       JSONB NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (resource_type, resource_id)
);
CREATE INDEX idx_fhir_identifier ON fhir_resources(identifier_system, identifier_value);

CREATE VIEW vw_historial_paciente AS
SELECT p.id AS paciente_id, p.rut, p.nombre_completo,
       e.id AS examen_id, e.tipo_examen, e.fecha, e.hora, e.estado, e.sucursal, e.profesional
FROM pacientes p
JOIN examenes e ON e.paciente_id = p.id;

-- Roles base de la aplicación
INSERT INTO roles (id, nombre, descripcion) VALUES
 ('ROL_TENS','TENS','Registra y corrige exámenes'),
 ('ROL_OFT','OFTALMOLOGO','Valida, observa y entrega exámenes'),
 ('ROL_ADMIN','ADMINISTRADOR','Gestiona usuarios y permisos'),
 ('ROL_PAC','PACIENTE','Solo consulta sus exámenes')
ON CONFLICT (id) DO NOTHING;
