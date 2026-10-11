package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;

public class MissingCookieSecurityRule implements SecurityRule {
    @Override public String id() { return "SEC-010"; }
    @Override public String name() { return "Missing Explicit Security Configuration for Demo Case"; }
    @Override public Severity severity() { return Severity.LOW; }
    @Override public String description() { return "A deliberately narrow completeness check for this educational project: it expects explicit session-cookie security settings when scanning a Spring Boot application configuration."; }
    @Override public String recommendation() { return "For this demo, explicitly configure Secure and HttpOnly session-cookie properties and document the deployment assumption."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        List<ConfigEntry> entries = RuleSupport.configEntries(project);
        if (entries.isEmpty()) {
            return List.of();
        }

        boolean securePresent = entries.stream().anyMatch(entry ->
                entry.key().equalsIgnoreCase("server.servlet.session.cookie.secure"));
        boolean httpOnlyPresent = entries.stream().anyMatch(entry ->
                entry.key().equalsIgnoreCase("server.servlet.session.cookie.http-only"));

        if (securePresent && httpOnlyPresent) {
            return List.of();
        }

        String file = entries.stream().findFirst().map(ConfigEntry::file).orElse("<no application configuration>");
        Integer line = entries.stream().findFirst().map(ConfigEntry::lineNumber).orElse(null);
        return List.of(new Finding(
                id(),
                name(),
                severity(),
                file,
                line,
                "Explicit session-cookie security configuration is incomplete for this demo profile",
                description(),
                recommendation()));
    }
}
