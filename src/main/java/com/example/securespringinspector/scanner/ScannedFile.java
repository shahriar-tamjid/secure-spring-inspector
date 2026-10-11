package com.example.securespringinspector.scanner;

import com.example.securespringinspector.model.ConfigEntry;
import com.example.securespringinspector.model.FileType;

import java.util.List;

public record ScannedFile(
        String relativePath,
        FileType fileType,
        List<String> lines,
        List<ConfigEntry> configEntries) {
}
