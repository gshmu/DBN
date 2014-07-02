package com.dci.intellij.dbn.code.common.style.options;

public enum CodeStyleCase {
    PRESERVE ("Preserve case"),
    UPPER("Upper case"),
    LOWER("Lower case"),
    CAPITALIZED("Capitalized");

    private String displayName;

    private CodeStyleCase(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
