package com.example.securespringinspector;

import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.rule.RuleEngine;
import com.example.securespringinspector.scanner.ProjectScanException;
import com.example.securespringinspector.scanner.ProjectScanner;
import com.example.securespringinspector.scanner.ScannedProject;
import com.example.securespringinspector.parser.ConfigFileParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectScannerIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void vulnerableAndSecureFixturesProduceExpectedResults() throws IOException {
        Path vulnerable = createFixture("vulnerable",
                "debug=true\n"
                        + "app.cors.allowed-origins=*\n"
                        + "management.endpoints.web.exposure.include=*\n"
                        + "app.datasource.password=secret123\n"
                        + "remote.service.url=http://example.com/api\n"
                        + "server.servlet.session.cookie.secure=false\n"
                        + "server.servlet.session.cookie.http-only=false\n"
                        + "app.upload.directory=../uploads\n",
                "package demo;\npublic class SecurityConfig {\n  void configure() { http.csrf(csrf -> csrf.disable()); }\n}\n");

        Path secure = createFixture("secure",
                "debug=false\n"
                        + "app.cors.allowed-origins=https://example.com\n"
                        + "management.endpoints.web.exposure.include=health,info\n"
                        + "app.datasource.password=${DB_PASSWORD}\n"
                        + "remote.service.url=https://example.com/api\n"
                        + "server.servlet.session.cookie.secure=true\n"
                        + "server.servlet.session.cookie.http-only=true\n"
                        + "app.upload.directory=uploads\n",
                "package demo;\npublic class SecurityConfig {\n}\n");

        ProjectScanner scanner = new ProjectScanner(new ConfigFileParser());
        RuleEngine engine = new RuleEngine();

        var vulnerableResult = engine.inspect(scanner.scan(vulnerable));
        var secureResult = engine.inspect(scanner.scan(secure));

        assertTrue(vulnerableResult.findings().stream().anyMatch(f -> f.ruleId().equals("SEC-004") && f.severity() == Severity.HIGH));
        assertTrue(vulnerableResult.findings().stream().anyMatch(f -> f.ruleId().equals("SEC-007")));
        assertTrue(vulnerableResult.findings().size() >= 8);
        assertEquals(0, secureResult.findings().size());
    }


    @Test
    void emptyProjectIsHandledWithoutFindings() throws IOException {
        Path empty = Files.createDirectory(tempDir.resolve("empty"));
        ProjectScanner scanner = new ProjectScanner(new ConfigFileParser());
        var result = new RuleEngine().inspect(scanner.scan(empty));
        assertEquals(0, result.filesAnalysed());
        assertEquals(0, result.findings().size());
    }

    @Test
    void missingTargetPathIsAnInputError() {
        ProjectScanner scanner = new ProjectScanner(new ConfigFileParser());
        assertThrows(ProjectScanException.class, () -> scanner.scan(tempDir.resolve("does-not-exist")));
    }

    private Path createFixture(String name, String properties, String javaSource) throws IOException {
        Path root = tempDir.resolve(name);
        Files.createDirectories(root.resolve("src/main/java/demo"));
        Files.writeString(root.resolve("application.properties"), properties);
        Files.writeString(root.resolve("src/main/java/demo/SecurityConfig.java"), javaSource);
        Files.writeString(root.resolve("README.txt"), "unsupported");
        return root;
    }
}
