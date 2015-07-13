package com.dci.intellij.dbn.debugger;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;

public abstract class DebugCommandRunner extends DebuggerCommandImpl{
    private  DebugProcessImpl debugProcess;

    public DebugCommandRunner(DebugProcessImpl debugProcess) {
        this.debugProcess = debugProcess;
    }

    public final void invoke() {
        debugProcess.getManagerThread().invoke(this);
    }

    public final void invokeAndWait() {
        debugProcess.getManagerThread().invokeAndWait(this);
    }
}
