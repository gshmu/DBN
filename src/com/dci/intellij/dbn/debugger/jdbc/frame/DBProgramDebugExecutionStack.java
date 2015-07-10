package com.dci.intellij.dbn.debugger.jdbc.frame;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.debugger.jdbc.process.DBProgramDebugProcess;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

public class DBProgramDebugExecutionStack extends XExecutionStack {
    private DBProgramDebugStackFrame topStackFrame;
    private DBProgramDebugProcess debugProcess;

    protected DBProgramDebugExecutionStack(DBProgramDebugProcess debugProcess) {
        super(debugProcess.getName(), debugProcess.getIcon());
        this.debugProcess = debugProcess;
        ExecutionBacktraceInfo backtraceInfo = debugProcess.getBacktraceInfo();
        int frameNumber = backtraceInfo == null ? 1 : backtraceInfo.getTopFrameIndex();
        topStackFrame = new DBProgramDebugStackFrame(debugProcess, debugProcess.getRuntimeInfo(), frameNumber);

    }



    @Override
    public XStackFrame getTopFrame() {
        return topStackFrame;
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        List<DBProgramDebugStackFrame> frames = new ArrayList<DBProgramDebugStackFrame>();
        ExecutionBacktraceInfo backtraceInfo = debugProcess.getBacktraceInfo();
        if (backtraceInfo != null) {
            for (DebuggerRuntimeInfo runtimeInfo : backtraceInfo.getFrames()) {
                if (StringUtil.isNotEmpty(runtimeInfo.getOwnerName()) || debugProcess.getExecutionInput() instanceof StatementExecutionInput) {
                    DBProgramDebugStackFrame frame = new DBProgramDebugStackFrame(debugProcess, runtimeInfo, runtimeInfo.getFrameIndex());
                    frames.add(frame);
                }
            }
            container.addStackFrames(frames, true) ;
        }
    }
}
