package com.equipo7.oftafhir.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.DiagnosticReport;

/**
 * Persistencia de recursos FHIR en PostgreSQL/Neon.
 * Si no hay NEON_DATABASE_URL configurada, los proveedores siguen usando su mapa en memoria.
 */
@Service
public class NeonStore {
    public static final String RUT_SYSTEM = "https://oftaapp.demo/identifier/rut";
    public static final String EXAM_ID_SYSTEM = "https://oftaapp.demo/identifier/examen";

    private final JdbcTemplate jdbc;
    private final boolean activo;
    private final FhirContext contexto = FhirContext.forR4();

    public NeonStore(JdbcTemplate jdbc, @Value("${NEON_DATABASE_URL:}") String urlNeon) {
        this.jdbc = jdbc;
        this.activo = urlNeon != null && !urlNeon.isBlank();
    }

    public boolean activo() {
        return activo;
    }

    public String guardar(String tipo, String id, String sistema, String valor,
                          String sujeto, String estado, String json) {
        String idFinal = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        jdbc.update("""
                INSERT INTO fhir_resources
                  (resource_type, resource_id, identifier_system, identifier_value,
                   subject_reference, status, payload, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CAST(? AS jsonb), now())
                ON CONFLICT (resource_type, resource_id) DO UPDATE SET
                  identifier_system = EXCLUDED.identifier_system,
                  identifier_value = EXCLUDED.identifier_value,
                  subject_reference = EXCLUDED.subject_reference,
                  status = EXCLUDED.status,
                  payload = EXCLUDED.payload,
                  updated_at = now()
                """, tipo, idFinal, sistema, valor, sujeto, estado, json);
        try {
            sincronizarTablasApp(tipo, idFinal, json);
        } catch (Exception e) {
            // El recurso FHIR ya quedó guardado; un fallo en las tablas de la app no lo revierte.
            System.out.println("NeonStore: tablas app no sincronizadas: " + e.getMessage());
        }
        return idFinal;
    }

    /** Espejo en las tablas de la app (pacientes / examenes) usando los mismos datos del recurso FHIR. */
    private void sincronizarTablasApp(String tipo, String id, String json) {
        if ("Patient".equals(tipo)) {
            Patient p = deJson(Patient.class, json);
            UUID idApp = UUID.nameUUIDFromBytes(("pac-" + id).getBytes());
            String rut = p.hasIdentifier() && p.getIdentifierFirstRep().hasValue()
                    ? p.getIdentifierFirstRep().getValue() : "";
            String nombre = p.hasName() ? p.getNameFirstRep().getText() : "";
            String fecha = p.hasBirthDate() ? p.getBirthDateElement().getValueAsString() : null;
            String correo = null, telefono = null;
            if (p.hasTelecom()) {
                for (var cp : p.getTelecom()) {
                    if ("email".equals(cp.getSystem())) correo = cp.getValue();
                    if ("phone".equals(cp.getSystem())) telefono = cp.getValue();
                }
            }
            jdbc.update("""
                INSERT INTO pacientes (id, rut, nombre_completo, fecha_nacimiento, edad, prevision,
                                       telefono, email, activo, created_at, updated_at)
                VALUES (?, ?, ?, ?, NULL, 'Sin especificar', ?, ?, TRUE, now(), now())
                ON CONFLICT (id) DO UPDATE SET rut=EXCLUDED.rut, nombre_completo=EXCLUDED.nombre_completo,
                    fecha_nacimiento=EXCLUDED.fecha_nacimiento, telefono=EXCLUDED.telefono,
                    email=EXCLUDED.email, updated_at=now()
                """, idApp.toString(), rut, nombre, fecha == null ? null : java.sql.Date.valueOf(fecha),
                    telefono, correo);
        } else if ("DiagnosticReport".equals(tipo)) {
            DiagnosticReport r = deJson(DiagnosticReport.class, json);
            jdbc.update("ALTER TABLE examenes ADD COLUMN IF NOT EXISTS hora_examen VARCHAR(5)");
            UUID idApp = UUID.nameUUIDFromBytes(("exa-" + id).getBytes());
            UUID idPaciente = UUID.nameUUIDFromBytes(("pac-" + r.getSubject().getReference()
                    .replace("Patient/", "")).getBytes());
            String tipoExamen = r.hasCode() && r.getCode().getText() != null
                    ? r.getCode().getText() : "Examen oftalmológico";
            String efectivo = r.hasEffective() && r.getEffective() instanceof org.hl7.fhir.r4.model.DateTimeType
                    ? r.getEffectiveDateTimeType().getValueAsString() : "";
            String fecha = efectivo.length() >= 10 ? efectivo.substring(0, 10) : null;
            String hora = efectivo.length() >= 16 ? efectivo.substring(11, 16) : null;
            String conclusion = r.hasConclusion() ? r.getConclusion() : "";
            String[] lineas = conclusion.split("\\R");
            String observaciones = lineas.length > 0 ? lineas[0] : "";
            String sucursal = extraer(conclusion, "Sucursal: ");
            String ojo = extraer(conclusion, "Ojo: ");
            String estado = mapEstado(r.getStatus().toCode());
            jdbc.update("""
                INSERT INTO examenes (id, paciente_id, tipo_examen, fecha_examen, hora_examen,
                                      ojo_evaluado, sucursal, estado, observaciones,
                                      created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now())
                ON CONFLICT (id) DO UPDATE SET tipo_examen=EXCLUDED.tipo_examen,
                    fecha_examen=EXCLUDED.fecha_examen, hora_examen=EXCLUDED.hora_examen,
                    ojo_evaluado=EXCLUDED.ojo_evaluado, sucursal=EXCLUDED.sucursal,
                    estado=EXCLUDED.estado, observaciones=EXCLUDED.observaciones, updated_at=now()
                """, idApp.toString(), idPaciente.toString(), tipoExamen,
                    fecha == null ? null : java.sql.Date.valueOf(fecha), hora, ojo, sucursal,
                    estado, observaciones);
        }
    }

    private String extraer(String texto, String prefijo) {
        for (String linea : texto.split("\\R")) {
            if (linea.startsWith(prefijo)) return linea.substring(prefijo.length()).trim();
        }
        return "";
    }

    private String mapEstado(String codigo) {
        return switch (codigo) {
            case "final" -> "VALIDADO";
            case "corrected" -> "OBSERVADO";
            case "amended" -> "ENTREGADO";
            default -> "PENDIENTE";
        };
    }

    public boolean existe(String tipo, String id) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM fhir_resources WHERE resource_type = ? AND resource_id = ?",
                Integer.class, tipo, id);
        return n != null && n > 0;
    }

    public String leerJson(String tipo, String id) {
        List<String> filas = jdbc.query(
                "SELECT payload::text FROM fhir_resources WHERE resource_type = ? AND resource_id = ?",
                (rs, i) -> rs.getString(1), tipo, id);
        return filas.isEmpty() ? null : filas.get(0);
    }

    public List<String> buscarPorIdentificador(String tipo, String sistema, String valor) {
        return jdbc.query("""
                SELECT payload::text FROM fhir_resources
                WHERE resource_type = ? AND identifier_system = ? AND identifier_value = ?
                ORDER BY updated_at DESC
                """, (rs, i) -> rs.getString(1), tipo, sistema, valor);
    }

    public List<String> buscarTodos(String tipo) {
        return jdbc.query("SELECT payload::text FROM fhir_resources WHERE resource_type = ?",
                (rs, i) -> rs.getString(1), tipo);
    }

    public String aJson(org.hl7.fhir.r4.model.Resource recurso) {
        return contexto.newJsonParser().encodeResourceToString(recurso);
    }

    public <T extends org.hl7.fhir.r4.model.Resource> T deJson(Class<T> clase, String json) {
        return contexto.newJsonParser().parseResource(clase, json);
    }
}
