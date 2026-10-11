package com.example.securespringinspector.rule;

import com.example.securespringinspector.model.Finding;
import com.example.securespringinspector.scanner.ScannedProject;

import java.util.List;

public interface SecurityRule {
    String id();
    String name();
    com.example.securespringinspector.model.Severity severity();
    String description();
    String recommendation();
    List<Finding> evaluate(ScannedProject project);
}
