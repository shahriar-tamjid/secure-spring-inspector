package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;

public class DebugModeRule implements SecurityRule {
    @Override public String id() { return "SEC-001"; }
    @Override public String name() { return "Debug Mode Enabled"; }
    @Override public Severity severity() { return Severity.LOW; }
    @Override public String description() { return "Checks supported application configuration for an explicitly enabled debug flag."; }
    @Override public String recommendation() { return "Disable debug mode in production-facing configuration unless it is explicitly required for a controlled environment."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForConfig(this, project,
                entry -> entry.key().equalsIgnoreCase("debug") && RuleSupport.isTruthy(entry.value()),
                "Debug mode is explicitly enabled");
    }
}
