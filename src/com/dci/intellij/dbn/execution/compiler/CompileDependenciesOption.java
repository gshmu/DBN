package com.dci.intellij.dbn.execution.compiler;

public enum CompileDependenciesOption {
    YES("Yes"),
    NO("No"),
    ASK("Ask");

    private String displayName;

    CompileDependenciesOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CompileDependenciesOption get(String name) {
        for (CompileDependenciesOption compileDependenciesOption : CompileDependenciesOption.values()) {
            if (compileDependenciesOption.getDisplayName().equals(name) || compileDependenciesOption.name().equals(name)) {
                return compileDependenciesOption;
            }
        }
        return null;
    }}
