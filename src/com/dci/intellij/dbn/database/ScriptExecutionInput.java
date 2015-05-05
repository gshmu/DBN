package com.dci.intellij.dbn.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptExecutionInput {
    private String command;
    private String content;
    private String[] environmentVariables;

    public ScriptExecutionInput(@NotNull String command, @NotNull String content, @Nullable String[] environmentVariables) {
        this.command = command;
        this.content = content;
        this.environmentVariables = environmentVariables;
    }

    public String getContent() {
        return content;
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
