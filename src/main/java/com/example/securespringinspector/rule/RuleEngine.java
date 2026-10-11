package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.InspectionResult;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class RuleEngine {

    private final List<SecurityRule> rules;

    public RuleEngine() {
        this.rules = List.of(
                new DebugModeRule(),
                new WildcardCorsRule(),
                new BroadActuatorExposureRule(),
                new HardCodedSecretRule(),
                new InsecureHttpUrlRule(),
                new WeakCookieSecurityRule(),
                new DisabledCsrfRule(),
                new SensitiveActuatorEndpointRule(),
                new UnsafeUploadDirectoryRule(),
                new MissingCookieSecurityRule());
    }

    public InspectionResult inspect(ScannedProject project) {
        Set<Finding> distinct = new LinkedHashSet<>();
        for (SecurityRule rule : rules) {
            distinct.addAll(rule.evaluate(project));
        }

        List<Finding> findings = new ArrayList<>(distinct);
        findings.sort(Comparator
                .comparing((Finding finding) -> finding.severity().rank())
                .thenComparing(Finding::ruleId)
                .thenComparing(Finding::file)
                .thenComparing(finding -> finding.lineNumber() == null ? Integer.MAX_VALUE : finding.lineNumber()));

        return new InspectionResult(
                project.root(),
                project.files().size(),
                rules.size(),
                findings,
                project.warnings());
    }

    public List<SecurityRule> rules() {
        return rules;
    }
}
