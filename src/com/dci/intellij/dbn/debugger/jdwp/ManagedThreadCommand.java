package com.dci.intellij.dbn.debugger.jdwp;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;

public abstract class ManagedThreadCommand extends DebuggerCommandImpl{
    private DebugProcessImpl debugProcess;
    private Priority priority;

    public ManagedThreadCommand(DebugProcessImpl debugProcess) {
        this(debugProcess, Priority.LOW);
    }
    public ManagedThreadCommand(DebugProcessImpl debugProcess, Priority priority) {
        this.debugProcess = debugProcess;
        this.priority = priority;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    public final void schedule() {
        schedule(priority);
    }

    public final void schedule(Priority priority) {
        this.priority = priority;
        debugProcess.getManagerThread().schedule(this);
    }

    public final void invoke() {
        invoke(priority);
    }
    public final void invoke(Priority priority) {
        this.priority = priority;
        debugProcess.getManagerThread().invoke(this);
    }

    public final void invokeAndWait() {
        debugProcess.getManagerThread().invokeAndWait(this);
    }

    @Deprecated
    public void execute() {
        try {
            action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
