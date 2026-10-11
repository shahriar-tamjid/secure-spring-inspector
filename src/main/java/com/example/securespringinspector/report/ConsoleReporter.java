package com.example.securespringinspector.report;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.InspectionResult;
import com.example.securespringinspector.model.Severity;

public class ConsoleReporter implements Reporter {

    @Override
    public void report(InspectionResult result, Severity failOn) {
        System.out.println("SecureSpring Inspector");
        System.out.println("======================");
        System.out.println("Target: " + result.target());
        System.out.println("Files analysed: " + result.filesAnalysed());
        System.out.println("Rules evaluated: " + result.rulesEvaluated());
        System.out.println();

        if (result.findings().isEmpty()) {
            System.out.println("No findings detected.");
        } else {
            for (Finding finding : result.findings()) {
                String line = finding.lineNumber() == null ? "" : ":" + finding.lineNumber();
                System.out.printf("%-6s %-8s %s (%s%s)%n",
                        finding.severity(),
                        finding.ruleId(),
                        finding.message(),
                        finding.file(),
                        line);
            }
        }

        System.out.println();
        System.out.println("Summary");
        System.out.println("-------");
        System.out.println("High: " + result.count(Severity.HIGH));
        System.out.println("Medium: " + result.count(Severity.MEDIUM));
        System.out.println("Low: " + result.count(Severity.LOW));
        long passed = Math.max(0, result.rulesEvaluated() - result.findings().stream()
                .map(Finding::ruleId)
                .distinct()
                .count());
        System.out.println("Passed rules: " + passed);
        if (!result.warnings().isEmpty()) {
            System.out.println("Warnings: " + result.warnings().size());
        }
        System.out.println("Fail threshold: " + failOn);
    }
}
