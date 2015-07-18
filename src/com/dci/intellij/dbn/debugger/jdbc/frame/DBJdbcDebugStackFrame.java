package com.dci.intellij.dbn.debugger.jdbc.frame;

import javax.swing.Icon;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugStackFrame;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBJdbcDebuggerEvaluator;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.impl.XSourcePositionImpl;

public class DBJdbcDebugStackFrame extends DBDebugStackFrame<DBJdbcDebugProcess> {
    private DBJdbcDebuggerEvaluator evaluator;
    private DebuggerRuntimeInfo runtimeInfo;

    public DBJdbcDebugStackFrame(DBJdbcDebugProcess debugProcess, DebuggerRuntimeInfo runtimeInfo, int index) {
        super(debugProcess, index);
        this.runtimeInfo = runtimeInfo;
    }

    @Override
    protected XSourcePosition computeSourcePosition() {
        DBJdbcDebugProcess debugProcess = getDebugProcess();
        VirtualFile virtualFile = debugProcess.getRuntimeInfoFile(runtimeInfo);

        int lineNumber = runtimeInfo.getLineNumber();
        if (runtimeInfo.getOwnerName() == null) {
            ExecutionInput executionInput = debugProcess.getExecutionInput();
            if (executionInput instanceof StatementExecutionInput) {
                StatementExecutionInput statementExecutionInput = (StatementExecutionInput) executionInput;
                lineNumber += statementExecutionInput.getExecutableLineNumber();
            }
        }
        return XSourcePositionImpl.create(virtualFile, lineNumber);
    }

    @Override
    protected VirtualFile getVirtualFile() {
        return getDebugProcess().getRuntimeInfoFile(runtimeInfo);
    }

    @NotNull
    @Override
    protected DBJdbcDebugValue createDebugValue(String variableName, Set<String> childVariableNames, Icon icon) {
        return new DBJdbcDebugValue(this, null, variableName, childVariableNames, icon);
    }

    @Nullable
    @Override
    protected DBDebugValue createSuspendReasonDebugValue() {
        return new DBSuspendReasonDebugValue(this);
    }

    @Override
    public XDebuggerEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = new DBJdbcDebuggerEvaluator(this);
        }
        return evaluator;
    }


    @Nullable
    @Override
    public Object getEqualityObject() {
        DebuggerRuntimeInfo runtimeInfo = getDebugProcess().getRuntimeInfo();
        return runtimeInfo == null ? null : runtimeInfo.getOwnerName() + "." + runtimeInfo.getProgramName();
    }
}


