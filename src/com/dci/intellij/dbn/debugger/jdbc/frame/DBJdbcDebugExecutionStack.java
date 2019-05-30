package com.dci.intellij.dbn.debugger.jdbc.frame;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

import java.util.ArrayList;
import java.util.List;

public class DBJdbcDebugExecutionStack extends XExecutionStack {
    private DBJdbcDebugStackFrame topStackFrame;
    private DBJdbcDebugProcess debugProcess;

    DBJdbcDebugExecutionStack(DBJdbcDebugProcess debugProcess) {
        super(debugProcess.getName(), debugProcess.getIcon());
        this.debugProcess = debugProcess;
        ExecutionBacktraceInfo backtraceInfo = debugProcess.getBacktraceInfo();
        int frameNumber = backtraceInfo == null ? 1 : backtraceInfo.getTopFrameIndex();
        topStackFrame = new DBJdbcDebugStackFrame(debugProcess, debugProcess.getRuntimeInfo(), frameNumber);

    }



    @Override
    public XStackFrame getTopFrame() {
        return topStackFrame;
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        List<DBJdbcDebugStackFrame> frames = new ArrayList<DBJdbcDebugStackFrame>();
        ExecutionBacktraceInfo backtraceInfo = debugProcess.getBacktraceInfo();
        if (backtraceInfo != null) {
            for (DebuggerRuntimeInfo runtimeInfo : backtraceInfo.getFrames()) {
                if (StringUtil.isNotEmpty(runtimeInfo.getOwnerName()) || debugProcess.getExecutionInput() instanceof StatementExecutionInput) {
                    DBJdbcDebugStackFrame frame = new DBJdbcDebugStackFrame(debugProcess, runtimeInfo, runtimeInfo.getFrameIndex());
                    frames.add(frame);
                }
            }
            container.addStackFrames(frames, true) ;
        }
    }
}
