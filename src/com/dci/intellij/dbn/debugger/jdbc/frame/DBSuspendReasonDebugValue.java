package com.dci.intellij.dbn.debugger.jdbc.frame;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.debugger.jdbc.process.DBProgramDebugProcess;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;

public class DBSuspendReasonDebugValue extends DBProgramDebugValue{
    public DBSuspendReasonDebugValue(DBProgramDebugProcess debugProcess, int frameIndex) {
        super(debugProcess, null, "DEBUG_RUNTIME_EVENT", null, Icons.EXEC_MESSAGES_INFO, frameIndex);
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        DebuggerRuntimeInfo runtimeInfo = getDebugProcess().getRuntimeInfo();
        String reason = "Unknown";
        if (runtimeInfo != null) {
            DatabaseDebuggerInterface debuggerInterface = getDebugProcess().getDebuggerInterface();
            reason = runtimeInfo.getReason() +" (" + debuggerInterface.getRuntimeEventReason(runtimeInfo.getReason()) + ")";
        }
        node.setPresentation(Icons.EXEC_MESSAGES_INFO, null, reason, false);
    }
}
