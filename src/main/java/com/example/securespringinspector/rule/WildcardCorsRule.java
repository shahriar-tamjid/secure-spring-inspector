package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.ArrayList;
import java.util.List;

public class WildcardCorsRule implements SecurityRule {
    @Override public String id() { return "SEC-002"; }
    @Override public String name() { return "Wildcard CORS"; }
    @Override public Severity severity() { return Severity.MEDIUM; }
    @Override public String description() { return "Checks for wildcard allowed-origin configuration in supported properties/YAML or obvious Java configuration calls."; }
    @Override public String recommendation() { return "Allow only the specific browser origins that the application actually needs."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        List<Finding> findings = new ArrayList<>();
        findings.addAll(RuleSupport.findingsForConfig(this, project,
                entry -> entry.key().toLowerCase().contains("cors")
                        && entry.key().toLowerCase().contains("origin")
                        && entry.value().trim().equals("*"),
                "Wildcard CORS origin is configured"));
        findings.addAll(RuleSupport.findingsForJavaLines(this, project,
                line -> line.contains("setAllowedOrigins") && line.contains("\"*\""),
                "Wildcard CORS origin is configured in Java source"));
        return findings;
    }
}
