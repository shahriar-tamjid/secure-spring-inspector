package com.example.securespringinspector.report;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.model.InspectionResult;
import com.example.securespringinspector.model.Severity;

public class JsonReporter implements Reporter {

    @Override
    public void report(InspectionResult result, Severity failOn) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"target\": \"").append(escape(result.target().toString())).append("\",\n");
        json.append("  \"filesAnalysed\": ").append(result.filesAnalysed()).append(",\n");
        json.append("  \"rulesEvaluated\": ").append(result.rulesEvaluated()).append(",\n");
        json.append("  \"failOn\": \"").append(failOn).append("\",\n");
        json.append("  \"findings\": [\n");

        for (int i = 0; i < result.findings().size(); i++) {
            Finding finding = result.findings().get(i);
            json.append("    {\n");
            json.append("      \"ruleId\": \"").append(escape(finding.ruleId())).append("\",\n");
            json.append("      \"ruleName\": \"").append(escape(finding.ruleName())).append("\",\n");
            json.append("      \"severity\": \"").append(finding.severity()).append("\",\n");
            json.append("      \"file\": \"").append(escape(finding.file())).append("\",\n");
            if (finding.lineNumber() == null) {
                json.append("      \"lineNumber\": null,\n");
            } else {
                json.append("      \"lineNumber\": ").append(finding.lineNumber()).append(",\n");
            }
            json.append("      \"message\": \"").append(escape(finding.message())).append("\",\n");
            json.append("      \"description\": \"").append(escape(finding.description())).append("\",\n");
            json.append("      \"recommendation\": \"").append(escape(finding.recommendation())).append("\"\n");
            json.append("    }");
            if (i < result.findings().size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }

        json.append("  ],\n");
        json.append("  \"summary\": {\n");
        json.append("    \"high\": ").append(result.count(Severity.HIGH)).append(",\n");
        json.append("    \"medium\": ").append(result.count(Severity.MEDIUM)).append(",\n");
        json.append("    \"low\": ").append(result.count(Severity.LOW)).append("\n");
        json.append("  },\n");
        json.append("  \"warnings\": [");
        for (int i = 0; i < result.warnings().size(); i++) {
            if (i > 0) {
                json.append(", ");
            }
            json.append('\"').append(escape(result.warnings().get(i))).append('\"');
        }
        json.append("]\n");
        json.append("}\n");

        System.out.print(json);
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
