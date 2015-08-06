package com.dci.intellij.dbn.debugger.jdwp.frame;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

public class DBJdwpDebugExecutionStack extends XExecutionStack {
    private DBJdwpDebugSuspendContext suspendContext;
    private LazyValue<DBJdwpDebugStackFrame> topStackFrame = new SimpleLazyValue<DBJdwpDebugStackFrame>() {
        @Override
        protected DBJdwpDebugStackFrame load() {
            XExecutionStack underlyingStack = getUnderlyingStack();
            XStackFrame topFrame = underlyingStack == null ? null : underlyingStack.getTopFrame();
            DBJdwpDebugProcess debugProcess = suspendContext.getDebugProcess();
            return new DBJdwpDebugStackFrame(debugProcess, topFrame, 1);
        }
    };

    @Nullable
    private XExecutionStack getUnderlyingStack() {
        return suspendContext.getUnderlyingContext().getActiveExecutionStack();
    }

    public DBJdwpDebugExecutionStack(DBJdwpDebugSuspendContext suspendContext) {
        super(suspendContext.getDebugProcess().getName(), suspendContext.getDebugProcess().getIcon());
        this.suspendContext = suspendContext;
    }

    public DBJdwpDebugSuspendContext getSuspendContext() {
        return suspendContext;
    }

    @Override
    public XStackFrame getTopFrame() {
        return topStackFrame.get();
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, final XStackFrameContainer container) {
        XExecutionStack underlyingStack = getUnderlyingStack();
        if (underlyingStack != null) {
            XStackFrameContainer fakeContainer = new XStackFrameContainer() {

                @Override
                public void addStackFrames(@NotNull List<? extends XStackFrame> stackFrames, boolean last) {
                    DBJdwpDebugProcess debugProcess = suspendContext.getDebugProcess();
                    List<DBJdwpDebugStackFrame> frames = new ArrayList<DBJdwpDebugStackFrame>();
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
            underlyingStack.computeStackFrames(0, fakeContainer);

        }
    }
}
