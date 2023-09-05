package com.dci.intellij.dbn.debugger.jdwp.frame;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.jdwp.ManagedThreadCommand;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.debugger.impl.PrioritizedTask;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBJdwpDebugExecutionStack extends XExecutionStack {
    private final DBJdwpDebugSuspendContext suspendContext;
    private final List<DBJdwpDebugStackFrame> stackFrames = CollectionUtil.createConcurrentList();

    private final Latent<DBJdwpDebugStackFrame> topStackFrame = Latent.basic(() -> {
        XExecutionStack underlyingStack = getUnderlyingStack();
        XStackFrame topFrame = underlyingStack == null ? null : underlyingStack.getTopFrame();
        return getFrame((JavaStackFrame) topFrame);
    });

    @Nullable
    private XExecutionStack getUnderlyingStack() {
        return suspendContext.getUnderlyingContext().getActiveExecutionStack();
    }

    DBJdwpDebugExecutionStack(DBJdwpDebugSuspendContext suspendContext) {
        // WORKAROUND hide the single value "threads" dropdown
        // super(suspendContext.getDebugProcess().getName(), suspendContext.getDebugProcess().getIcon());
        super("", null);
        this.suspendContext = suspendContext;
    }

    private DBJdwpDebugSuspendContext getSuspendContext() {
        return suspendContext;
    }

    @Override
    public XStackFrame getTopFrame() {
        return topStackFrame.get();
    }

    private DBJdwpDebugStackFrame getFrame(JavaStackFrame underlyingFrame) {
        for (DBJdwpDebugStackFrame stackFrame : stackFrames) {
            if (stackFrame.getUnderlyingFrame().equals(underlyingFrame)) {
                return stackFrame;
            }
        }

        DBJdwpDebugProcess debugProcess = suspendContext.getDebugProcess();
        DBJdwpDebugStackFrame stackFrame = new DBJdwpDebugStackFrame(debugProcess, underlyingFrame, stackFrames.size());
        stackFrames.add(stackFrame);
        return stackFrame;
    }

    @Override
    public void computeStackFrames(final int firstFrameIndex, final XStackFrameContainer container) {
        DebugProcessImpl debugProcess = getSuspendContext().getDebugProcess().getDebuggerSession().getProcess();
        final XExecutionStack underlyingStack = getUnderlyingStack();
        if (underlyingStack != null) {
            ManagedThreadCommand.schedule(debugProcess, PrioritizedTask.Priority.LOW, () -> {
                XStackFrameContainer fakeContainer = new XStackFrameContainer() {
                    @Override
                    public void addStackFrames(@NotNull List<? extends XStackFrame> stackFrames, boolean last) {
                        if (stackFrames.size() > 0) {
                            List<DBJdwpDebugStackFrame> frames = new ArrayList<>();
                            for (XStackFrame underlyingFrame : stackFrames) {
                                DBJdwpDebugStackFrame frame = getFrame((JavaStackFrame) underlyingFrame);
                                if (frame != null) {
                                    XSourcePosition sourcePosition = frame.getSourcePosition();
                                    //VirtualFile virtualFile = DBDebugUtil.getSourceCodeFile(sourcePosition);
                                    //DBSchemaObject object = DBDebugUtil.getObject(sourcePosition);
                                    frames.add(frame);
                                    last = last || DBDebugUtil.getObject(sourcePosition) == null;
                                }
                            }
                            if (frames.size() > 0) {
                                container.addStackFrames(frames, last) ;
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
            });
        }
    }
}
