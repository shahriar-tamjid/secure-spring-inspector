package com.example.securespringinspector.model;

import java.nio.file.Path;
import java.util.List;

public record InspectionResult(
        Path target,
        int filesAnalysed,
        int rulesEvaluated,
        List<Finding> findings,
        List<String> warnings) {

    public InspectionResult {
        findings = List.copyOf(findings);
        warnings = List.copyOf(warnings);
    }

    public long count(Severity severity) {
        return findings.stream().filter(finding -> finding.severity() == severity).count();
    }
}
