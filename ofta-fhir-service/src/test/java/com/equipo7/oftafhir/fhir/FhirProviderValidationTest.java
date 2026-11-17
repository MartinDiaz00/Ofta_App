package com.equipo7.oftafhir.fhir;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FhirProviderValidationTest {

    @Test
    void patientSinNombreEsRechazado() {
        PatientProvider provider = new PatientProvider();
        Patient patient = new Patient();
        assertThrows(InvalidRequestException.class, () -> provider.create(patient));
    }

    @Test
    void patientValidoSeCreaConId() {
        PatientProvider provider = new PatientProvider();
        Patient patient = new Patient();
        patient.addName().setFamily("Rojas").addGiven("Camila");
        var outcome = provider.create(patient);
        assertTrue(outcome.getCreated());
        assertNotNull(outcome.getId());
        assertEquals("Patient", outcome.getId().getResourceType());
    }

    @Test
    void patientSeBuscaPorRutComoIdentificador() {
        PatientProvider provider = new PatientProvider();
        Patient patient = new Patient();
        patient.addName().setText("Camila Rojas");
        patient.addIdentifier()
                .setSystem("https://oftaapp.demo/identifier/rut")
                .setValue("15.790.867-2");
        provider.create(patient);

        assertEquals(1, provider.search(null,
                new StringParam("https://oftaapp.demo/identifier/rut|15.790.867-2")).size());
        assertTrue(provider.search(null, new StringParam("99.999.999-9")).isEmpty());
    }

    @Test
    void diagnosticReportRequiereSubject() {
        DiagnosticReportProvider provider = new DiagnosticReportProvider();
        DiagnosticReport report = new DiagnosticReport();
        report.setStatus(DiagnosticReport.DiagnosticReportStatus.REGISTERED);
        report.setCode(new CodeableConcept().setText("Fondo de ojo"));
        assertThrows(InvalidRequestException.class, () -> provider.create(report));
    }

    @Test
    void diagnosticReportValidoSeCrea() {
        DiagnosticReportProvider provider = new DiagnosticReportProvider();
        DiagnosticReport report = new DiagnosticReport();
        report.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
        report.setCode(new CodeableConcept().setText("Fondo de ojo"));
        report.setSubject(new Reference("Patient/1001"));
        report.setConclusion("Sin hallazgos relevantes.");
        var outcome = provider.create(report);
        assertTrue(outcome.getCreated());
        assertNotNull(outcome.getResource());
    }

    @Test
    void diagnosticReportSeBuscaPorIdentificadorYSeActualizaSinDuplicar() {
        DiagnosticReportProvider provider = new DiagnosticReportProvider();
        String sistema = "https://oftaapp.demo/identifier/examen";

        DiagnosticReport report = new DiagnosticReport();
        report.setStatus(DiagnosticReport.DiagnosticReportStatus.REGISTERED);
        report.setCode(new CodeableConcept().setText("Fondo de ojo"));
        report.setSubject(new Reference("Patient/1001"));
        report.addIdentifier().setSystem(sistema).setValue("exa-1");
        String id = provider.create(report).getId().getIdPart();

        assertEquals(1, provider.search(null, null, new TokenParam(sistema, "exa-1")).size());

        DiagnosticReport validado = new DiagnosticReport();
        validado.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
        validado.setCode(new CodeableConcept().setText("Fondo de ojo"));
        validado.setSubject(new Reference("Patient/1001"));
        validado.addIdentifier().setSystem(sistema).setValue("exa-1");
        provider.update(new IdType(id), validado);

        assertEquals(DiagnosticReport.DiagnosticReportStatus.FINAL, provider.read(new IdType(id)).getStatus());
        assertEquals(1, provider.search(null, null, new TokenParam(sistema, "exa-1")).size(),
                "Sigue existiendo un único recurso tras actualizar");
    }
}
