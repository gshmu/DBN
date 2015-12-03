package com.dci.intellij.dbn.debugger.jdwp;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;

public abstract class ManagedThreadCommand extends DebuggerCommandImpl{
    private  DebugProcessImpl debugProcess;

    public ManagedThreadCommand(DebugProcessImpl debugProcess) {
        this.debugProcess = debugProcess;
    }


    public final void schedule() {
        debugProcess.getManagerThread().schedule(this);
    }

    public final void invoke() {
        debugProcess.getManagerThread().invoke(this);
    }

    public final void invokeAndWait() {
        debugProcess.getManagerThread().invokeAndWait(this);
    }
}
