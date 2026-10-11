package com.example.securespringinspector.parser;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.FileType;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Lightweight line-oriented configuration parser. It intentionally supports
 * only simple key/value mappings so its behaviour remains predictable.
 */
@Component
public class ConfigFileParser {

    public List<ConfigEntry> parse(List<String> lines, String file, FileType type) {
        return type == FileType.PROPERTIES
                ? parseProperties(lines, file)
                : parseYaml(lines, file);
    }

    private List<ConfigEntry> parseProperties(List<String> lines, String file) {
        List<ConfigEntry> entries = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                continue;
            }

            int separator = findPropertySeparator(line);
            if (separator <= 0) {
                continue;
            }

            String key = line.substring(0, separator).trim();
            String value = stripQuotes(line.substring(separator + 1).trim());
            entries.add(new ConfigEntry(key, value, i + 1, file, raw));
        }
        return entries;
    }

    private List<ConfigEntry> parseYaml(List<String> lines, String file) {
        List<ConfigEntry> entries = new ArrayList<>();
        Deque<YamlLevel> stack = new ArrayDeque<>();

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String lineWithoutComment = stripComment(raw);
            if (lineWithoutComment.trim().isEmpty()) {
                continue;
            }

            String trimmed = lineWithoutComment.trim();
            if (trimmed.equals("---") || trimmed.equals("...") || trimmed.startsWith("- ")) {
                continue;
            }

            int indent = countLeadingSpaces(lineWithoutComment);
            int separator = lineWithoutComment.indexOf(':');
            if (separator <= 0) {
                throw new IllegalArgumentException(
                        "Unsupported or malformed YAML at " + file + ":" + (i + 1));
            }

            while (!stack.isEmpty() && indent <= stack.peek().indent()) {
                stack.pop();
            }

            String keyPart = lineWithoutComment.substring(0, separator).trim();
            String valuePart = lineWithoutComment.substring(separator + 1).trim();
            String fullKey = buildKey(stack, keyPart);

            if (valuePart.isEmpty()) {
                stack.push(new YamlLevel(indent, keyPart));
                continue;
            }

            entries.add(new ConfigEntry(fullKey, stripQuotes(valuePart), i + 1, file, raw));
        }

        return entries;
    }

    private String buildKey(Deque<YamlLevel> stack, String key) {
        if (stack.isEmpty()) {
            return key;
        }

        List<String> parents = new ArrayList<>();
        for (YamlLevel level : stack) {
            parents.add(level.key());
        }
        java.util.Collections.reverse(parents);
        parents.add(key);
        return String.join(".", parents);
    }

    private int findPropertySeparator(String line) {
        int equals = line.indexOf('=');
        int colon = line.indexOf(':');
        if (equals < 0) {
            return colon;
        }
        if (colon < 0) {
            return equals;
        }
        return Math.min(equals, colon);
    }

    private int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private String stripComment(String line) {
        boolean single = false;
        boolean doubled = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\'' && !doubled) {
                single = !single;
            } else if (c == '"' && !single) {
                doubled = !doubled;
            } else if (c == '#' && !single && !doubled) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private record YamlLevel(int indent, String key) {
    }
}
