package com.example.securespringinspector.scanner;

import com.example.securespringinspector.model.FileType;
import com.example.securespringinspector.parser.ConfigFileParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class ProjectScanner {

    private static final long MAX_FILE_SIZE_BYTES = 1_000_000;

    private final ConfigFileParser configFileParser;

    public ProjectScanner(ConfigFileParser configFileParser) {
        this.configFileParser = configFileParser;
    }

    public ScannedProject scan(Path root) {
        validateRoot(root);

        List<ScannedFile> files = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(root)) {
            List<Path> candidates = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> !containsIgnoredDirectory(path, root))
                    .filter(path -> isSupported(path, root))
                    .sorted(Comparator.comparing(path -> root.relativize(path).toString()))
                    .toList();

            for (Path path : candidates) {
                String relative = root.relativize(path).toString().replace('\\', '/');
                FileType type = fileType(path);
                try {
                    if (Files.size(path) > MAX_FILE_SIZE_BYTES) {
                        warnings.add("Skipped large file (> 1 MB): " + relative);
                        continue;
                    }

                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                    List<com.example.securespringinspector.model.ConfigEntry> entries = List.of();
                    if (type == FileType.PROPERTIES || type == FileType.YAML) {
                        try {
                            entries = configFileParser.parse(lines, relative, type);
                        } catch (IllegalArgumentException exception) {
                            warnings.add(exception.getMessage());
                        }
                    }
                    files.add(new ScannedFile(relative, type, lines, entries));
                } catch (IOException exception) {
                    warnings.add("Could not read " + relative + ": " + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw new ProjectScanException("Could not inspect target project: " + exception.getMessage(), exception);
        }

        return new ScannedProject(root, List.copyOf(files), List.copyOf(warnings));
    }

    private void validateRoot(Path root) {
        if (!Files.exists(root)) {
            throw new ProjectScanException("Target path does not exist: " + root);
        }
        if (!Files.isDirectory(root)) {
            throw new ProjectScanException("Target path is not a directory: " + root);
        }
    }

    private boolean isSupported(Path path, Path root) {
        String fileName = path.getFileName().toString();
        if (fileName.equals("application.properties")
                || fileName.equals("application.yml")
                || fileName.equals("application.yaml")
                || fileName.equals("pom.xml")) {
            return true;
        }
        return fileName.endsWith(".java") && root.relativize(path).toString().contains("src");
    }

    private FileType fileType(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.equals("application.properties")) {
            return FileType.PROPERTIES;
        }
        if (fileName.equals("application.yml") || fileName.equals("application.yaml")) {
            return FileType.YAML;
        }
        if (fileName.equals("pom.xml")) {
            return FileType.POM_XML;
        }
        return FileType.JAVA;
    }

    private boolean containsIgnoredDirectory(Path path, Path root) {
        Path relative = root.relativize(path);
        for (Path part : relative) {
            if (part.toString().equals("target") || part.toString().equals(".git")) {
                return true;
            }
        }
        return false;
    }
}
