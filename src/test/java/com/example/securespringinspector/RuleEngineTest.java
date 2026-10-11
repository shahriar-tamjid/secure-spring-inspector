package com.example.securespringinspector;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.FileType;
import com.example.securespringinspector.rule.RuleEngine;
import com.example.securespringinspector.scanner.ScannedFile;
import com.example.securespringinspector.scanner.ScannedProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleEngineTest {

    @Test
    void deDuplicatesIdenticalFindings() {
        ConfigEntry debug = new ConfigEntry("debug", "true", 1, "application.properties", "debug=true");
        ScannedFile sameFile = new ScannedFile("application.properties", FileType.PROPERTIES,
                List.of("debug=true"), List.of(debug));

        ScannedProject project = new ScannedProject(
                Path.of("test-project"),
                List.of(sameFile, sameFile),
                List.of());

        var result = new RuleEngine().inspect(project);
        assertEquals(1, result.findings().stream()
                .filter(f -> f.ruleId().equals("SEC-001"))
                .count());
    }
}
