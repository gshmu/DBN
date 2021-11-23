package com.dci.intellij.dbn.diagnostics.data;

import lombok.Getter;

@Getter
public enum DiagnosticCategory {
    DATABASE("Database Diagnostics"),
    PARSER("Parser Diagnostics");

    private final String name;

    DiagnosticCategory(String name) {
        this.name = name;
    }
}
