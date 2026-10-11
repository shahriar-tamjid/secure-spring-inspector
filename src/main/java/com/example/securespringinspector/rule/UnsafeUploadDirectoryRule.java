package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;

public class UnsafeUploadDirectoryRule implements SecurityRule {
    @Override public String id() { return "SEC-009"; }
    @Override public String name() { return "Unsafe File/Upload Configuration"; }
    @Override public Severity severity() { return Severity.MEDIUM; }
    @Override public String description() { return "Checks a clearly defined upload-directory configuration for parent-directory traversal or filesystem-root style paths."; }
    @Override public String recommendation() { return "Use a dedicated application-owned upload directory and validate file names and storage boundaries in the application."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForConfig(this, project,
                entry -> {
                    String key = entry.key().toLowerCase();
                    String value = entry.value().trim();
                    if (!(key.contains("upload") && key.contains("dir"))) {
                        return false;
                    }
                    String normalized = value.replace('\\', '/');
                    return normalized.equals("/")
                            || normalized.equals("C:/")
                            || normalized.startsWith("../")
                            || normalized.startsWith("../../")
                            || normalized.contains("/../");
                },
                "Upload directory points outside an obviously local application-owned path");
    }
}
