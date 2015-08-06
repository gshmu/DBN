package com.dci.intellij.dbn.execution;

public class ExecutionTimeout {
    private int value;
    private int settingsValue;

    public ExecutionTimeout(int value) {
        this.value = value;
        this.settingsValue = value;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
    }

    public void updateSettingsValue(int settingsValue) {
        if (this.value == this.settingsValue) {
            // is using settings value
            this.value = settingsValue;
        }
        this.settingsValue = settingsValue;
    }
}
