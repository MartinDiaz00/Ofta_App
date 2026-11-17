package com.equipo7.oftafhir.fhir;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
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

/**
 * Recurso FHIR Patient. Persiste en Neon (tabla fhir_resources) cuando hay conexión;
 * si no, usa almacenamiento en memoria para que el microservicio funcione sin BD.
 */
@Component
public class PatientProvider implements IResourceProvider {

    private final Map<String, Patient> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1000);

    @Autowired(required = false)
    private NeonStore neon;

    private boolean remoto() {
        return neon != null && neon.activo();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Create
    public MethodOutcome create(@ResourceParam Patient patient) {
        validate(patient);
        String id = patient.getIdElement().isEmpty() ? String.valueOf(seq.incrementAndGet()) : patient.getIdElement().getIdPart();
        patient.setId(id);
        if (patient.getMeta().isEmpty()) patient.getMeta().setLastUpdated(new Date());
        if (remoto()) {
            String rut = patient.hasIdentifier() && patient.getIdentifierFirstRep().hasValue()
                    ? patient.getIdentifierFirstRep().getValue() : "";
            id = neon.guardar("Patient", id, NeonStore.RUT_SYSTEM, rut, null, null, neon.aJson(patient));
            patient.setId(id);
        } else {
            store.put(id, patient.copy());
        }
        MethodOutcome outcome = new MethodOutcome(new IdType("Patient", id));
        outcome.setResource(patient);
        outcome.setCreated(true);
        return outcome;
    }

    @Read
    public Patient read(@IdParam IdType id) {
        if (remoto()) {
            String json = neon.leerJson("Patient", id.getIdPart());
            if (json == null) throw new ResourceNotFoundException(id);
            return neon.deJson(Patient.class, json);
        }
        Patient p = store.get(id.getIdPart());
        if (p == null) throw new ResourceNotFoundException(id);
        return p.copy();
    }

    @Update
    public MethodOutcome update(@IdParam IdType id, @ResourceParam Patient patient) {
        if (remoto()) {
            if (!neon.existe("Patient", id.getIdPart())) throw new ResourceNotFoundException(id);
        } else if (!store.containsKey(id.getIdPart())) {
            throw new ResourceNotFoundException(id);
        }
        validate(patient);
        patient.setId(id.getIdPart());
        patient.getMeta().setLastUpdated(new Date());
        if (remoto()) {
            String rut = patient.hasIdentifier() && patient.getIdentifierFirstRep().hasValue()
                    ? patient.getIdentifierFirstRep().getValue() : "";
            neon.guardar("Patient", id.getIdPart(), NeonStore.RUT_SYSTEM, rut, null, null, neon.aJson(patient));
        } else {
            store.put(id.getIdPart(), patient.copy());
        }
        MethodOutcome outcome = new MethodOutcome(id);
        outcome.setResource(patient);
        return outcome;
    }

    @Search
    public List<Patient> search(
            @OptionalParam(name = Patient.SP_NAME) StringParam name,
            @OptionalParam(name = Patient.SP_IDENTIFIER) StringParam identifier) {
        List<Patient> pacientes;
        if (remoto()) {
            List<String> json;
            if (identifier != null && identifier.getValue() != null && !identifier.getValue().isBlank()) {
                String valor = identifier.getValue();
                String sistema = NeonStore.RUT_SYSTEM;
                if (valor.contains("|")) {
                    String[] partes = valor.split("\\|", 2);
                    sistema = partes[0];
                    valor = partes[1];
                }
                json = neon.buscarPorIdentificador("Patient", sistema, valor);
            } else {
                json = neon.buscarTodos("Patient");
            }
            pacientes = json.stream().map(p -> neon.deJson(Patient.class, p)).collect(Collectors.toList());
        } else {
            pacientes = new ArrayList<>(store.values());
        }
        return pacientes.stream()
                .filter(p -> matchesName(p, name))
                .filter(p -> matchesIdentifier(p, identifier))
                .map(Patient::copy)
                .collect(Collectors.toList());
    }

    private void validate(Patient patient) {
        boolean hasName = patient.hasName() && patient.getName().stream()
                .anyMatch(n -> n.hasFamily() || n.hasGiven() || n.hasText());
        if (!hasName) throw new InvalidRequestException("Patient.name es obligatorio.");
    }

    private boolean matchesName(Patient p, StringParam name) {
        if (name == null) return true;
        String value = name.getValue();
        if (value == null || value.isBlank()) return true;
        String q = value.toLowerCase(Locale.ROOT);
        return p.getName().stream().anyMatch(n ->
                (n.hasText() && n.getText().toLowerCase(Locale.ROOT).contains(q)) ||
                        (n.hasFamily() && n.getFamily().toLowerCase(Locale.ROOT).contains(q)) ||
                        n.getGiven().stream().anyMatch(g -> g.getValue() != null && g.getValue().toLowerCase(Locale.ROOT).contains(q)));
    }

    private boolean matchesIdentifier(Patient p, StringParam identifier) {
        if (identifier == null) return true;
        String q = identifier.getValue();
        if (q == null || q.isBlank()) return true;
        final String valorBusqueda = q.contains("|") ? q.split("\\|", 2)[1] : q;
        return p.getIdentifier().stream().anyMatch(i ->
                valorBusqueda.equals(i.getValue()) || valorBusqueda.equals(i.getSystem() + "|" + i.getValue()));
    }
}
