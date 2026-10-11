package com.example.securespringinspector.model;

public record ConfigEntry(
        String key,
        String value,
        int lineNumber,
        String file,
        String originalLine) {
}
