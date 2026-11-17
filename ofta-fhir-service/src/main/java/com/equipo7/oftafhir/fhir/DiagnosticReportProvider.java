package com.equipo7.oftafhir.fhir;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/** Recurso FHIR DiagnosticReport con persistencia en Neon y fallback en memoria. */
@Component
public class DiagnosticReportProvider implements IResourceProvider {

    private final Map<String, DiagnosticReport> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(2000);

    @Autowired(required = false)
    private NeonStore neon;

    private boolean remoto() {
        return neon != null && neon.activo();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return DiagnosticReport.class;
    }

    @Create
    public MethodOutcome create(@ResourceParam DiagnosticReport report) {
        validate(report);
        String id = report.getIdElement().isEmpty() ? String.valueOf(seq.incrementAndGet()) : report.getIdElement().getIdPart();
        report.setId(id);
        if (report.getStatus() == null) report.setStatus(DiagnosticReport.DiagnosticReportStatus.REGISTERED);
        report.getMeta().setLastUpdated(new Date());
        if (remoto()) {
            id = guardarRemoto(id, report);
            report.setId(id);
        } else {
            store.put(id, report.copy());
        }
        MethodOutcome outcome = new MethodOutcome(new IdType("DiagnosticReport", id));
        outcome.setResource(report);
        outcome.setCreated(true);
        return outcome;
    }

    @Read
    public DiagnosticReport read(@IdParam IdType id) {
        if (remoto()) {
            String json = neon.leerJson("DiagnosticReport", id.getIdPart());
            if (json == null) throw new ResourceNotFoundException(id);
            return neon.deJson(DiagnosticReport.class, json);
        }
        DiagnosticReport r = store.get(id.getIdPart());
        if (r == null) throw new ResourceNotFoundException(id);
        return r.copy();
    }

    @Update
    public MethodOutcome update(@IdParam IdType id, @ResourceParam DiagnosticReport report) {
        if (remoto()) {
            if (!neon.existe("DiagnosticReport", id.getIdPart())) throw new ResourceNotFoundException(id);
        } else if (!store.containsKey(id.getIdPart())) {
            throw new ResourceNotFoundException(id);
        }
        validate(report);
        report.setId(id.getIdPart());
        report.getMeta().setLastUpdated(new Date());
        if (remoto()) {
            guardarRemoto(id.getIdPart(), report);
        } else {
            store.put(id.getIdPart(), report.copy());
        }
        MethodOutcome outcome = new MethodOutcome(id);
        outcome.setResource(report);
        return outcome;
    }

    private String guardarRemoto(String id, DiagnosticReport report) {
        String identificador = "";
        if (report.hasIdentifier() && report.getIdentifierFirstRep().hasValue()) {
            identificador = report.getIdentifierFirstRep().getValue();
        }
        String sujeto = report.hasSubject() && report.getSubject().getReference() != null
                ? report.getSubject().getReference() : null;
        String estado = report.hasStatus() ? report.getStatus().toCode() : null;
        return neon.guardar("DiagnosticReport", id, NeonStore.EXAM_ID_SYSTEM, identificador,
                sujeto, estado, neon.aJson(report));
    }

    @Search
    public List<DiagnosticReport> search(
            @OptionalParam(name = DiagnosticReport.SP_SUBJECT) ReferenceParam subject,
            @OptionalParam(name = DiagnosticReport.SP_STATUS) TokenParam status,
            @OptionalParam(name = DiagnosticReport.SP_IDENTIFIER) TokenParam identifier) {
        List<DiagnosticReport> reportes;
        if (remoto()) {
            List<String> json;
            if (identifier != null && identifier.getValue() != null && !identifier.getValue().isBlank()) {
                String valor = identifier.getValue();
                String valorBusqueda = valor.contains("|") ? valor.split("\\|", 2)[1] : valor;
                json = neon.buscarPorIdentificador("DiagnosticReport", NeonStore.EXAM_ID_SYSTEM, valorBusqueda);
            } else {
                json = neon.buscarTodos("DiagnosticReport");
            }
            reportes = json.stream().map(r -> neon.deJson(DiagnosticReport.class, r)).collect(Collectors.toList());
        } else {
            reportes = new ArrayList<>(store.values());
        }
        return reportes.stream()
                .filter(r -> matchesSubject(r, subject))
                .filter(r -> matchesStatus(r, status))
                .filter(r -> matchesIdentifier(r, identifier))
                .map(DiagnosticReport::copy)
                .collect(Collectors.toList());
    }

    private void validate(DiagnosticReport report) {
        if (!report.hasStatus()) report.setStatus(DiagnosticReport.DiagnosticReportStatus.REGISTERED);
        if (!report.hasCode() || (report.getCode().getText() == null && !report.getCode().hasCoding())) {
            report.setCode(new CodeableConcept().setText("Examen oftalmológico"));
        }
        if (!report.hasSubject()) {
            throw new InvalidRequestException("DiagnosticReport.subject (paciente) es obligatorio.");
        }
    }

    private boolean matchesSubject(DiagnosticReport r, ReferenceParam subject) {
        if (subject == null) return true;
        String value = subject.getValue();
        if (value == null || value.isBlank()) return true;
        String q = value.replace("Patient/", "");
        return r.hasSubject() && (q.equals(r.getSubject().getReference()) ||
                r.getSubject().getReference().endsWith("/" + q) ||
                r.getSubject().getReference().equals("Patient/" + q));
    }

    private boolean matchesStatus(DiagnosticReport r, TokenParam status) {
        if (status == null) return true;
        String value = status.getValue();
        if (value == null || value.isBlank()) return true;
        return r.hasStatus() && value.equalsIgnoreCase(r.getStatus().toCode());
    }

    private boolean matchesIdentifier(DiagnosticReport r, TokenParam identifier) {
        if (identifier == null) return true;
        String value = identifier.getValue();
        if (value == null || value.isBlank()) return true;
        final String valorBusqueda = value.contains("|") ? value.split("\\|", 2)[1] : value;
        return r.hasIdentifier() && r.getIdentifier().stream()
                .anyMatch(i -> valorBusqueda.equals(i.getValue()));
    }
}
