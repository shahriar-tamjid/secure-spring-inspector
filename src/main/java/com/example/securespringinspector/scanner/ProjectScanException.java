package com.example.securespringinspector.scanner;

public class ProjectScanException extends RuntimeException {
    public ProjectScanException(String message) {
        super(message);
    }

    public ProjectScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
