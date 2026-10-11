package com.example.securespringinspector.cli;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.InspectionResult;
import com.example.securespringinspector.model.Severity;
import com.example.securespringinspector.report.ConsoleReporter;
import com.example.securespringinspector.report.JsonReporter;
import com.example.securespringinspector.report.Reporter;
import com.example.securespringinspector.scanner.ProjectScanException;
import com.example.securespringinspector.scanner.ProjectScanner;
import com.example.securespringinspector.scanner.ScannedProject;
import com.example.securespringinspector.rule.RuleEngine;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Component
public class InspectorCommandLineRunner implements CommandLineRunner {

    private final ApplicationArguments args;
    private final ProjectScanner scanner;
    private final RuleEngine ruleEngine;
    private int exitCode = 0;

    public InspectorCommandLineRunner(
            ApplicationArguments args,
            ProjectScanner scanner,
            RuleEngine ruleEngine) {
        this.args = args;
        this.scanner = scanner;
        this.ruleEngine = ruleEngine;
    }

    @Override
    public void run(String... ignored) {
        List<String> positional = args.getNonOptionArgs();
        if (positional.size() != 1) {
            printUsage("Expected exactly one target project path.");
            exitCode = 2;
            return;
        }

        String format = getOption("format", "console").toLowerCase(Locale.ROOT);
        String failOnText = getOption("fail-on", "LOW").toUpperCase(Locale.ROOT);

        final Severity failOn;
        try {
            failOn = Severity.valueOf(failOnText);
        } catch (IllegalArgumentException exception) {
            printUsage("Invalid --fail-on value. Use HIGH, MEDIUM, or LOW.");
            exitCode = 2;
            return;
        }

        if (!format.equals("console") && !format.equals("json")) {
            printUsage("Invalid --format value. Use console or json.");
            exitCode = 2;
            return;
        }

        Path target = Path.of(positional.get(0)).toAbsolutePath().normalize();

        try {
            ScannedProject project = scanner.scan(target);
            InspectionResult result = ruleEngine.inspect(project);

            Reporter reporter = format.equals("json") ? new JsonReporter() : new ConsoleReporter();
            reporter.report(result, failOn);
            exitCode = hasFindingAtOrAbove(result.findings(), failOn) ? 1 : 0;
        } catch (ProjectScanException exception) {
            System.err.println("Scanner error: " + exception.getMessage());
            exitCode = 2;
        }
    }

    public int getExitCode() {
        return exitCode;
    }

    private String getOption(String name, String defaultValue) {
        List<String> values = args.getOptionValues(name);
        return values == null || values.isEmpty() ? defaultValue : values.get(0);
    }

    private boolean hasFindingAtOrAbove(List<Finding> findings, Severity threshold) {
        return findings.stream().anyMatch(finding -> finding.severity().rank() <= threshold.rank());
    }

    private void printUsage(String error) {
        System.err.println("Scanner error: " + error);
        System.err.println("Usage: java -jar securespring-inspector.jar <project-path> [--format=console|json] [--fail-on=LOW|MEDIUM|HIGH]");
    }
}
