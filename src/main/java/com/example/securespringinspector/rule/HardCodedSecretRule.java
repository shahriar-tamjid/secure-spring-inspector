package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;
import java.util.regex.Pattern;

public class HardCodedSecretRule implements SecurityRule {
    private static final Pattern SECRET_KEY = Pattern.compile(
            "(^|[.\\-_])(password|passwd|secret|token|api[-.]?key|access[-.]?key)([.\\-_]|$)",
            Pattern.CASE_INSENSITIVE);

    @Override public String id() { return "SEC-004"; }
    @Override public String name() { return "Hard-coded Password/Secret-like Property"; }
    @Override public Severity severity() { return Severity.HIGH; }
    @Override public String description() { return "Checks supported configuration for secret-like keys that contain a literal value rather than a placeholder."; }
    @Override public String recommendation() { return "Move secrets to environment variables or a managed secret store and reference them through placeholders."; }

    @Override
    public List<Finding> evaluate(ScannedProject project) {
        return RuleSupport.findingsForConfig(this, project, this::isLiteralSecret, "Hard-coded secret-like value detected");
    }

    private boolean isLiteralSecret(ConfigEntry entry) {
        String key = entry.key().trim();
        String value = entry.value().trim();
        boolean keyLooksSensitive = SECRET_KEY.matcher(key).find();
        return keyLooksSensitive && !RuleSupport.looksLikeTemplate(value);
    }
}
