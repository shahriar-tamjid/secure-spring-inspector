package com.example.securespringinspector.report;

import com.example.securespringinspector.model.InspectionResult;
import com.example.securespringinspector.model.Severity;

public interface Reporter {
    void report(InspectionResult result, Severity failOn);
}
