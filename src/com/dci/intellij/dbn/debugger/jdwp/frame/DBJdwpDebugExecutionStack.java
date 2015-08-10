package com.dci.intellij.dbn.debugger.jdwp.frame;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.debugger.engine.events.DebuggerContextCommandImpl;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

public class DBJdwpDebugExecutionStack extends XExecutionStack {
    private DBJdwpDebugSuspendContext suspendContext;
    private List<DBJdwpDebugStackFrame> stackFrames = new ArrayList<DBJdwpDebugStackFrame>();

    private LazyValue<DBJdwpDebugStackFrame> topStackFrame = new SimpleLazyValue<DBJdwpDebugStackFrame>() {
        @Override
        protected DBJdwpDebugStackFrame load() {
            XExecutionStack underlyingStack = getUnderlyingStack();
            XStackFrame topFrame = underlyingStack == null ? null : underlyingStack.getTopFrame();
            return getFrame((JavaStackFrame) topFrame);
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

    private synchronized DBJdwpDebugStackFrame getFrame(JavaStackFrame underlyingFrame) {
        for (DBJdwpDebugStackFrame stackFrame : stackFrames) {
            if (stackFrame.getUnderlyingFrame().equals(underlyingFrame)) {
                return stackFrame;
            }
        }

        DBJdwpDebugProcess debugProcess = suspendContext.getDebugProcess();
        DBJdwpDebugStackFrame stackFrame = new DBJdwpDebugStackFrame(debugProcess, underlyingFrame, stackFrames.size());
        if (stackFrame.getVirtualFile() != null) {
            stackFrames.add(stackFrame);
            return stackFrame;
        }
        else {
            return null;
        }
    }

    @Override
    public void computeStackFrames(final int firstFrameIndex, final XStackFrameContainer container) {
        DebugProcessImpl debugProcess = getSuspendContext().getDebugProcess().getDebuggerSession().getProcess();
        final XExecutionStack underlyingStack = getUnderlyingStack();
        if (underlyingStack != null) {
            debugProcess.getManagerThread().schedule(new DebuggerContextCommandImpl(debugProcess.getDebuggerContext()) {
                @Override
                public Priority getPriority() {
                    return Priority.NORMAL;
                }

                @Override
                public void threadAction() {
                    XStackFrameContainer fakeContainer = new XStackFrameContainer() {
                        @Override
                        public void addStackFrames(@NotNull List<? extends XStackFrame> stackFrames, boolean last) {
                            if (stackFrames.size() > 0) {
                                List<DBJdwpDebugStackFrame> frames = new ArrayList<DBJdwpDebugStackFrame>();
                                for (XStackFrame underlyingFrame : stackFrames) {
                                    DBJdwpDebugStackFrame frame = getFrame((JavaStackFrame) underlyingFrame);
                                    if (frame != null) {
                                        frames.add(frame);
                                    }
                                }
                                if (frames.size() > 0) {
                                    container.addStackFrames(frames, true) ;
                                }
                            }
                        }

                        @Override
                        public boolean isObsolete() {
                            return container.isObsolete();
                        }

                        @Override
                        public void errorOccurred(@NotNull String errorMessage) {

                        }
                    };
                    underlyingStack.computeStackFrames(firstFrameIndex, fakeContainer);
                }
            });
        }
    }
}
