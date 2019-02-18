package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.option.InteractiveOption;
import org.jetbrains.annotations.NotNull;

public enum CompileDependenciesOption implements InteractiveOption {
    YES("Yes", true),
    NO("No", true),
    ASK("Ask", false);

    private String name;
    private boolean persistable;

    CompileDependenciesOption(String name, boolean persistable) {
        this.name = name;
        this.persistable = persistable;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public boolean isCancel() {
        return false;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }


    public static CompileDependenciesOption get(String name) {
        for (CompileDependenciesOption compileDependenciesOption : CompileDependenciesOption.values()) {
            if (compileDependenciesOption.name.equals(name) || compileDependenciesOption.name().equals(name)) {
                return compileDependenciesOption;
            }
        }
        return null;
    }}
