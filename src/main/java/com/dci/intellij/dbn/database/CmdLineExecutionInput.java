package com.dci.intellij.dbn.database;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdLineExecutionInput {
    private StringBuilder content;
    private List<String> command;
    private Map<String, String> environmentVars;

    public CmdLineExecutionInput(@NotNull String content) {
        this.content = new StringBuilder(content);
        this.command = new ArrayList<String>();
        this.environmentVars = new HashMap<String, String>();

    }
    public StringBuilder getContent() {
        return content;
    }

    public String getTextContent() {
        return content.toString();
    }

    @NotNull
    public List<String> getCommand() {
        return command;
    }

    @NotNull
    public String getLineCommand() {
        StringBuilder lineCommand = new StringBuilder();
        for (String arg : command) {
            lineCommand.append(arg).append(" ");
        }

        return lineCommand.toString();
    }

    @NotNull
    public Map<String, String> getEnvironmentVars() {
        return environmentVars;
    }
}
