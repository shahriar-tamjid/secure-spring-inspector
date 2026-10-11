package com.example.securespringinspector.model;

public enum Severity {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int rank;

    Severity(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
