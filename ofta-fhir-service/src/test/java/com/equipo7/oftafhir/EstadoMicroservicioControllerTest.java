package com.equipo7.oftafhir;

import com.equipo7.oftafhir.api.EstadoMicroservicioController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EstadoMicroservicioControllerTest {

    @Test
    void statusDevuelveServicioOperativoYRutaFhir() {
        EstadoMicroservicioController controller = new EstadoMicroservicioController();
        ResponseEntity<Map<String, Object>> response = controller.status();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("/fhir", response.getBody().get("fhirBasePath"));
        assertEquals("R4", response.getBody().get("fhirVersion"));
        assertEquals("ofta-fhir-service", response.getBody().get("service"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
