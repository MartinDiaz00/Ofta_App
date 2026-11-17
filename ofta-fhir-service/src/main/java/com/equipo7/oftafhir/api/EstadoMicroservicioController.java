package com.equipo7.oftafhir.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Endpoint propio del microservicio (además de la API FHIR en /fhir).
 * Responsabilidad: exponer estado operativo y metadatos básicos para verificación/despliegue.
 */
@RestController
@RequestMapping("/api")
public class EstadoMicroservicioController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "service", "ofta-fhir-service",
                "status", "UP",
                "fhirBasePath", "/fhir",
                "fhirVersion", "R4",
                "timestamp", Instant.now().toString()
        ));
    }
}
