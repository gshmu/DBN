package com.dci.intellij.dbn.debugger.jdwp;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

public abstract class ManagedThreadCommand extends DebuggerCommandImpl{
    private Priority priority;

    private ManagedThreadCommand(Priority priority) {
        this.priority = priority;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Deprecated
    public void execute() {
        try {
            action();
        } catch (Exception e) {
            conditionallyLog(e);
        }
    }

    public static void schedule(DebugProcessImpl debugProcess, Priority priority, Runnable action) {
        ManagedThreadCommand command = create(priority, action);
        debugProcess.getManagerThread().schedule(command);
    }


    public static void invoke(DebugProcessImpl debugProcess, Priority priority, Runnable action) {
        ManagedThreadCommand command = create(priority, action);
        debugProcess.getManagerThread().invoke(command);
    }

    @NotNull
    private static ManagedThreadCommand create(Priority priority, Runnable action) {
        return new ManagedThreadCommand(priority) {
            @Override
            protected void action() throws Exception {
                action.run();
            }
        };
    }
}
