package com.dci.intellij.dbn.execution.logging;

public class LogOutput {
    private String text;
    private boolean addHeadline = false;

    public LogOutput(String text, boolean addHeadline) {
        this.text = text;
        this.addHeadline = addHeadline;
    }

    public String getText() {
        return text;
    }

    public boolean isAddHeadline() {
        return addHeadline;
    }
}
