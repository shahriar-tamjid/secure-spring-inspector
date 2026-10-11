package com.example.securespringinspector;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.FileType;
import com.example.securespringinspector.rule.*;
import com.example.securespringinspector.scanner.ScannedFile;
import com.example.securespringinspector.scanner.ScannedProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationRulesTest {

    @Test
    void detectsDebugButIgnoresComment() {
        ScannedProject project = configProject(
                "debug=true",
                "# debug=true");
        assertEquals(1, new DebugModeRule().evaluate(project).size());
    }

    @Test
    void detectsWildcardCorsAndAllowsSpecificOrigin() {
        assertEquals(1, new WildcardCorsRule().evaluate(configProject(
                "app.cors.allowed-origins=*",
                "# app.cors.allowed-origins=*" )).size());
        assertEquals(0, new WildcardCorsRule().evaluate(configProject(
                "app.cors.allowed-origins=https://example.com")).size());
    }

    @Test
    void detectsBroadActuatorExposure() {
        assertEquals(1, new BroadActuatorExposureRule().evaluate(configProject(
                "management.endpoints.web.exposure.include=*")).size());
        assertEquals(0, new BroadActuatorExposureRule().evaluate(configProject(
                "management.endpoints.web.exposure.include=health,info")).size());
    }

    @Test
    void detectsLiteralSecretButNotPlaceholder() {
        assertEquals(1, new HardCodedSecretRule().evaluate(configProject(
                "app.datasource.password=secret123")).size());
        assertEquals(0, new HardCodedSecretRule().evaluate(configProject(
                "app.datasource.password=${DB_PASSWORD}")).size());
    }

    @Test
    void detectsNonLocalHttpButIgnoresLocalHttp() {
        assertEquals(1, new InsecureHttpUrlRule().evaluate(configProject(
                "remote.service.url=http://example.com/api")).size());
        assertEquals(0, new InsecureHttpUrlRule().evaluate(configProject(
                "remote.service.url=http://localhost:8080/api")).size());
        assertEquals(1, new InsecureHttpUrlRule().evaluate(configProject(
                "remote.service.url=http://localhost.example.com/api")).size());
    }

    @Test
    void detectsWeakCookieSettings() {
        assertEquals(2, new WeakCookieSecurityRule().evaluate(configProject(
                "server.servlet.session.cookie.secure=false",
                "server.servlet.session.cookie.http-only=false")).size());
        assertEquals(0, new WeakCookieSecurityRule().evaluate(configProject(
                "server.servlet.session.cookie.secure=true",
                "server.servlet.session.cookie.http-only=true")).size());
    }

    @Test
    void detectsSensitiveActuatorEndpoints() {
        assertEquals(1, new SensitiveActuatorEndpointRule().evaluate(configProject(
                "management.endpoints.web.exposure.include=health,env")).size());
        assertEquals(0, new SensitiveActuatorEndpointRule().evaluate(configProject(
                "management.endpoints.web.exposure.include=health,info")).size());
    }

    @Test
    void detectsUnsafeUploadDirectory() {
        assertEquals(1, new UnsafeUploadDirectoryRule().evaluate(configProject(
                "app.upload.directory=../uploads")).size());
        assertEquals(0, new UnsafeUploadDirectoryRule().evaluate(configProject(
                "app.upload.directory=uploads")).size());
    }

    @Test
    void missingCookieRuleRequiresBothExplicitSettings() {
        assertEquals(1, new MissingCookieSecurityRule().evaluate(configProject(
                "spring.application.name=test")).size());
        assertEquals(0, new MissingCookieSecurityRule().evaluate(configProject(
                "server.servlet.session.cookie.secure=true",
                "server.servlet.session.cookie.http-only=true")).size());
    }

private ScannedProject configProject(String... lines) {
    List<ConfigEntry> entries = new java.util.ArrayList<>();
    for (int i = 0; i < lines.length; i++) {
        String line = lines[i];
        String trimmed = line.trim();

        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
            continue;
        }

        int equals = line.indexOf('=');
        String key = equals > 0 ? line.substring(0, equals) : line;
        String value = equals > 0 ? line.substring(equals + 1) : "";

        entries.add(new ConfigEntry(
                key,
                value,
                i + 1,
                "application.properties",
                line));
    }

    return new ScannedProject(
            Path.of("test-project"),
            List.of(new ScannedFile(
                    "application.properties",
                    FileType.PROPERTIES,
                    List.of(lines),
                    entries)),
            List.of());
}
}
