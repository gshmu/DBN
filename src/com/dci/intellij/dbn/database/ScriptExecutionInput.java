package com.dci.intellij.dbn.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptExecutionInput {
    private String command;
    private String[] environmentVariables;

    public ScriptExecutionInput(@NotNull String command, @Nullable String[] environmentVariables) {
        this.command = command;
        this.environmentVariables = environmentVariables;
    }

    @NotNull
    public String getCommand() {
        return command;
    }

    @Nullable
    public String[] getEnvironmentVariables() {
        return environmentVariables;
    }
}
