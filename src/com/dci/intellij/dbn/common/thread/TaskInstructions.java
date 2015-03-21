package com.dci.intellij.dbn.common.thread;

public class TaskInstructions {
    private String title;
    private boolean startInBackground;
    private boolean canBeCancelled;

    public TaskInstructions(String title, boolean startInBackground, boolean canBeCancelled) {
        this.title = title;
        this.startInBackground = startInBackground;
        this.canBeCancelled = canBeCancelled;
    }

    public String getTitle() {
        return title;
    }

    public boolean isStartInBackground() {
        return startInBackground;
    }

    public boolean isCanBeCancelled() {
        return canBeCancelled;
    }
}
