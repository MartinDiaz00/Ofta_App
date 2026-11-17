package com.equipo7.oftafhir.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import jakarta.servlet.Servlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OftaFhirServerConfig {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Bean
    public ServletRegistrationBean<Servlet> fhirServletRegistration(
            FhirContext fhirContext,
            PatientProvider patientProvider,
            DiagnosticReportProvider diagnosticReportProvider) {
        RestfulServer server = new RestfulServer(fhirContext);
        server.setResourceProviders(List.of(patientProvider, diagnosticReportProvider));
        server.registerInterceptor(new ResponseHighlighterInterceptor());
        ServletRegistrationBean<Servlet> registration = new ServletRegistrationBean<>(server, "/fhir/*");
        registration.setName("OftaFhirServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
