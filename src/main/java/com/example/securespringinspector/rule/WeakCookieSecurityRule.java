package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;

public class WeakCookieSecurityRule implements SecurityRule {
    @Override public String id() { return "SEC-006"; }
    @Override public String name() { return "Weak Cookie Security Setting"; }
    @Override public Severity severity() { return Severity.MEDIUM; }
    @Override public String description() { return "Checks explicit Spring session-cookie settings for secure=false or http-only=false."; }
    @Override public String recommendation() { return "For HTTPS deployments, keep the session cookie Secure and HttpOnly unless there is a documented reason not to."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForConfig(this, project,
                entry -> (entry.key().equalsIgnoreCase("server.servlet.session.cookie.secure")
                        || entry.key().equalsIgnoreCase("server.servlet.session.cookie.http-only"))
                        && entry.value().equalsIgnoreCase("false"),
                "Session cookie security is explicitly weakened");
    }
}
