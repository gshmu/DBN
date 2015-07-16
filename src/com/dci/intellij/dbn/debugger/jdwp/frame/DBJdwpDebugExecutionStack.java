package com.dci.intellij.dbn.debugger.jdwp.frame;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugProcess;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

public class DBJdwpDebugExecutionStack extends XExecutionStack {
    private DBJdwpDebugStackFrame topStackFrame;
    private DBJdwpDebugProcess debugProcess;
    private XExecutionStack underlyingStack;

    public DBJdwpDebugExecutionStack(DBJdwpDebugProcess debugProcess) {
        super(debugProcess.getName(), debugProcess.getIcon());
        this.debugProcess = debugProcess;
        underlyingStack = debugProcess.getSession().getSuspendContext().getActiveExecutionStack();
        XStackFrame topFrame = underlyingStack == null ? null : underlyingStack.getTopFrame();
        topStackFrame = new DBJdwpDebugStackFrame(debugProcess, topFrame, 1);

    }

    @Override
    public XStackFrame getTopFrame() {
        return topStackFrame;
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, final XStackFrameContainer container) {
        if (underlyingStack != null) {
            XStackFrameContainer undelyingContainer = new XStackFrameContainer() {

                @Override
                public void addStackFrames(@NotNull List<? extends XStackFrame> stackFrames, boolean last) {
                    final List<DBJdwpDebugStackFrame> frames = new ArrayList<DBJdwpDebugStackFrame>();
                    for (XStackFrame underlyingFrame : stackFrames) {
                        DBJdwpDebugStackFrame frame = new DBJdwpDebugStackFrame(debugProcess, underlyingFrame, frames.size());
                        if (frame.getVirtualFile() != null) {
                            frames.add(frame);
                        }
                    }
                    container.addStackFrames(frames, true) ;
                }

                @Override
                public boolean isObsolete() {
                    return false;
                }

                @Override
                public void errorOccurred(@NotNull String errorMessage) {

                }
            };
            underlyingStack.computeStackFrames(0, undelyingContainer);

        }
    }
}
