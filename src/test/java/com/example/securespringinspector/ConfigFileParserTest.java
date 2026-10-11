package com.example.securespringinspector;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.FileType;
import com.example.securespringinspector.parser.ConfigFileParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigFileParserTest {
    private final ConfigFileParser parser = new ConfigFileParser();

    @Test
    void parsesNestedYamlIntoDottedKeys() {
        List<ConfigEntry> entries = parser.parse(List.of(
                "server:",
                "  servlet:",
                "    session:",
                "      cookie:",
                "        secure: true",
                "        http-only: true"), "application.yml", FileType.YAML);

        assertEquals("server.servlet.session.cookie.secure", entries.get(0).key());
        assertEquals("true", entries.get(0).value());
        assertEquals(5, entries.get(0).lineNumber());
    }

    @Test
    void ignoresYamlComments() {
        List<ConfigEntry> entries = parser.parse(List.of(
                "# debug: true",
                "debug: false"), "application.yml", FileType.YAML);
        assertEquals(1, entries.size());
        assertEquals("false", entries.get(0).value());
    }

    @Test
    void rejectsUnsupportedMalformedYamlLine() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(
                List.of("debug true"), "application.yml", FileType.YAML));
    }
}
