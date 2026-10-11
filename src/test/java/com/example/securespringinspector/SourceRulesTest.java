package com.example.securespringinspector;

import com.example.securespringinspector.model.FileType;
import com.example.securespringinspector.rule.DisabledCsrfRule;
import com.example.securespringinspector.rule.WildcardCorsRule;
import com.example.securespringinspector.scanner.ScannedFile;
import com.example.securespringinspector.scanner.ScannedProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceRulesTest {

    @Test
    void detectsSupportedCsrfDisablePattern() {
        ScannedProject vulnerable = javaProject("config/SecurityConfig.java",
                "http.csrf(csrf -> csrf.disable());");
        assertEquals(1, new DisabledCsrfRule().evaluate(vulnerable).size());

        ScannedProject secure = javaProject("config/SecurityConfig.java",
                "http.csrf(csrf -> csrf.csrfTokenRepository(repository));");
        assertEquals(0, new DisabledCsrfRule().evaluate(secure).size());
    }

    @Test
    void detectsWildcardCorsJavaCallButNotComment() {
        ScannedProject project = javaProject("config/CorsConfig.java",
                "config.setAllowedOrigins(List.of(\"*\"));",
                "// config.setAllowedOrigins(List.of(\"*\"));");
        assertEquals(1, new WildcardCorsRule().evaluate(project).size());
    }

    private ScannedProject javaProject(String file, String... lines) {
        return new ScannedProject(
                Path.of("test-project"),
                List.of(new ScannedFile(file, FileType.JAVA, List.of(lines), List.of())),
                List.of());
    }
}
