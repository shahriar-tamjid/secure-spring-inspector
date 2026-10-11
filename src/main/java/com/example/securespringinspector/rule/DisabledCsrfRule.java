package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;

public class DisabledCsrfRule implements SecurityRule {
    @Override public String id() { return "SEC-007"; }
    @Override public String name() { return "Disabled CSRF Protection"; }
    @Override public Severity severity() { return Severity.HIGH; }
    @Override public String description() { return "Looks for a small set of explicit Spring Security CSRF-disable patterns in Java source."; }
    @Override public String recommendation() { return "Do not disable CSRF protection blindly. If the application is intentionally stateless, document and enforce an appropriate authentication design."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForJavaLines(this, project,
                line -> line.contains("csrf().disable()")
                        || line.contains("csrf(AbstractHttpConfigurer::disable)")
                        || line.contains("csrf(csrf -> csrf.disable())"),
                "Spring Security CSRF protection is explicitly disabled");
    }
}
