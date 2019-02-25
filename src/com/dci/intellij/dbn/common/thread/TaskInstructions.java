package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

@Deprecated
public class TaskInstructions extends PropertyHolderImpl<TaskInstruction> {
    private String title;

    private TaskInstructions(String title, TaskInstruction ... instructions) {
        this.title = title;
        if (instructions != null && instructions.length > 0) {
            for (TaskInstruction instruction : instructions) {
                if (instruction != null) {
                    set(instruction, true);
                }
            }
        }
    }

    @Override
    protected TaskInstruction[] properties() {
        return TaskInstruction.values();
    }

    public String getTitle() {
        return title;
    }

    public static TaskInstructions instructions(String title, TaskInstruction ... instructions) {
        return new TaskInstructions(title, instructions);
    }
}
