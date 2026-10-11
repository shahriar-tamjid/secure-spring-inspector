package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SensitiveActuatorEndpointRule implements SecurityRule {
    private static final Set<String> SENSITIVE = new HashSet<>(Set.of(
            "env", "beans", "configprops", "heapdump", "threaddump"));

    @Override public String id() { return "SEC-008"; }
    @Override public String name() { return "Exposed Management Endpoint Configuration"; }
    @Override public Severity severity() { return Severity.MEDIUM; }
    @Override public String description() { return "Checks whether a small list of sensitive actuator endpoints is explicitly exposed through web configuration."; }
    @Override public String recommendation() { return "Avoid exposing sensitive management endpoints publicly; use a minimal allow-list and protect operational interfaces."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForConfig(this, project,
                entry -> {
                    if (!entry.key().equalsIgnoreCase("management.endpoints.web.exposure.include")) {
                        return false;
                    }
                    return Arrays.stream(entry.value().split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .anyMatch(SENSITIVE::contains);
                },
                "A sensitive management endpoint is explicitly exposed");
    }
}
