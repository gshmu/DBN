package com.dci.intellij.dbn.debugger.jdbc.frame;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.intellij.xdebugger.frame.XExecutionStack;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DBJdbcDebugExecutionStack extends XExecutionStack {
    private final DBJdbcDebugStackFrame topFrame;
    private final DBJdbcDebugProcess debugProcess;

    DBJdbcDebugExecutionStack(DBJdbcDebugProcess debugProcess) {
        super(debugProcess.getName(), debugProcess.getIcon());
        this.debugProcess = debugProcess;
        ExecutionBacktraceInfo backtraceInfo = debugProcess.getBacktraceInfo();
        int frameNumber = backtraceInfo == null ? 1 : backtraceInfo.getTopFrameIndex();
        topFrame = new DBJdbcDebugStackFrame(debugProcess, debugProcess.getRuntimeInfo(), frameNumber);

    }


    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        List<DBJdbcDebugStackFrame> frames = new ArrayList<>();
        ExecutionBacktraceInfo backtraceInfo = debugProcess.getBacktraceInfo();
        if (backtraceInfo != null) {
            for (DebuggerRuntimeInfo runtimeInfo : backtraceInfo.getFrames()) {
                if (Strings.isNotEmpty(runtimeInfo.getOwnerName()) || debugProcess.getExecutionInput() instanceof StatementExecutionInput) {
                    DBJdbcDebugStackFrame frame = new DBJdbcDebugStackFrame(debugProcess, runtimeInfo, runtimeInfo.getFrameIndex());
                    frames.add(frame);
                }
            }
            container.addStackFrames(frames, true) ;
        }
    }
}
