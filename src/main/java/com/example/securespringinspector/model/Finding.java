package com.example.securespringinspector.model;

public record Finding(
        String ruleId,
        String ruleName,
        Severity severity,
        String file,
        Integer lineNumber,
        String message,
        String description,
        String recommendation) {
}
