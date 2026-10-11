package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsecureHttpUrlRule implements SecurityRule {
    private static final Pattern HTTP_URL = Pattern.compile("http://([^/\\s]+)", Pattern.CASE_INSENSITIVE);
    @Override public String id() { return "SEC-005"; }
    @Override public String name() { return "Insecure HTTP URL in Supported Configuration"; }
    @Override public Severity severity() { return Severity.MEDIUM; }
    @Override public String description() { return "Checks supported configuration values for literal http:// URLs, excluding localhost and loopback examples."; }
    @Override public String recommendation() { return "Prefer HTTPS for non-local services so traffic is protected in transit."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForConfig(this, project,
                entry -> containsNonLocalHttpUrl(entry.value()),
                "Non-local HTTP URL detected; HTTPS is not configured");
    }

    private boolean containsNonLocalHttpUrl(String value) {
        Matcher matcher = HTTP_URL.matcher(value);
        while (matcher.find()) {
            String hostPort = matcher.group(1);
            String host = hostPort.contains(":") ? hostPort.substring(0, hostPort.indexOf(':')) : hostPort;
            if (!host.equalsIgnoreCase("localhost")
                    && !host.equals("127.0.0.1")
                    && !host.equals("0.0.0.0")) {
                return true;
            }
        }
        return false;
    }
}
