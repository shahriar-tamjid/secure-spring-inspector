package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.FileType;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.scanner.ScannedFile;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

final class RuleSupport {

    private RuleSupport() {
    }

    static List<ConfigEntry> configEntries(ScannedProject project) {
        return project.files().stream()
                .filter(file -> file.fileType() == FileType.PROPERTIES || file.fileType() == FileType.YAML)
                .flatMap(file -> file.configEntries().stream())
                .toList();
    }

    static List<Finding> findingsForConfig(
            SecurityRule rule,
            ScannedProject project,
            Predicate<ConfigEntry> predicate,
            String message) {
        List<Finding> findings = new ArrayList<>();
        for (ConfigEntry entry : configEntries(project)) {
            if (predicate.test(entry)) {
                findings.add(new Finding(
                        rule.id(),
                        rule.name(),
                        rule.severity(),
                        entry.file(),
                        entry.lineNumber(),
                        message,
                        rule.description(),
                        rule.recommendation()));
            }
        }
        return findings;
    }

    static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    static boolean isTruthy(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1");
    }

    static boolean looksLikeTemplate(String value) {
        String v = value.trim();
        return v.isEmpty()
                || v.equals("null")
                || v.equals("****")
                || v.startsWith("${")
                || v.startsWith("#{")
                || v.startsWith("<")
                || v.equalsIgnoreCase("changeme")
                || v.equalsIgnoreCase("replace-me");
    }

    static List<Finding> findingsForJavaLines(
            SecurityRule rule,
            ScannedProject project,
            Predicate<String> predicate,
            String message) {
        List<Finding> findings = new ArrayList<>();
        for (ScannedFile file : project.files()) {
            if (file.fileType() != FileType.JAVA) {
                continue;
            }
            for (int i = 0; i < file.lines().size(); i++) {
                String line = file.lines().get(i);
                if (isCommentOnlyJavaLine(line)) {
                    continue;
                }
                if (predicate.test(line)) {
                    findings.add(new Finding(
                            rule.id(),
                            rule.name(),
                            rule.severity(),
                            file.relativePath(),
                            i + 1,
                            message,
                            rule.description(),
                            rule.recommendation()));
                }
            }
        }
        return findings;
    }

    private static boolean isCommentOnlyJavaLine(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*");
    }
}
