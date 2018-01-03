package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class TaskInstructions extends PropertyHolderImpl<TaskInstruction> {
    private String title;

    public TaskInstructions(String title, TaskInstruction ... instructions) {
        this.title = title;
        if (instructions != null && instructions.length > 0) {
            for (TaskInstruction instruction : instructions) {
                set(instruction, true);
            }
        }
    }

    @Override
    protected TaskInstruction[] getProperties() {
        return TaskInstruction.values();
    }

    public String getTitle() {
        return title;
    }

    public boolean isStartInBackground() {
        return is(TaskInstruction.START_IN_BACKGROUND);
    }

    public boolean isCanBeCancelled() {
        return is(TaskInstruction.CAN_BE_CANCELLED);
    }
}
