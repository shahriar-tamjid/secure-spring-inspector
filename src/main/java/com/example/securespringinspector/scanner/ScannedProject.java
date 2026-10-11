package com.example.securespringinspector.scanner;

import java.nio.file.Path;
import java.util.List;

public record ScannedProject(
        Path root,
        List<ScannedFile> files,
        List<String> warnings) {
}
