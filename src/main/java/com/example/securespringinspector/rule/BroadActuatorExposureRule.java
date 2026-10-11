package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;

public class BroadActuatorExposureRule implements SecurityRule {
    @Override public String id() { return "SEC-003"; }
    @Override public String name() { return "Broad Actuator Exposure"; }
    @Override public Severity severity() { return Severity.MEDIUM; }
    @Override public String description() { return "Checks whether Spring Boot Actuator web exposure is configured to include every endpoint."; }
    @Override public String recommendation() { return "Expose only the actuator endpoints needed for operations, and protect them with appropriate access controls."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForConfig(this, project,
                entry -> entry.key().equalsIgnoreCase("management.endpoints.web.exposure.include")
                        && entry.value().trim().equals("*"),
                "Actuator web exposure includes all endpoints (*)");
    }
}
