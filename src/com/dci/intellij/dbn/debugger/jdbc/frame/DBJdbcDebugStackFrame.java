package com.dci.intellij.dbn.debugger.jdbc.frame;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugSourcePosition;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugStackFrame;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBJdbcDebuggerEvaluator;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class DBJdbcDebugStackFrame extends DBDebugStackFrame<DBJdbcDebugProcess, DBJdbcDebugValue> {
    private final DebuggerRuntimeInfo runtimeInfo;
    private final Latent<DBJdbcDebuggerEvaluator> evaluator =
            Latent.basic(() -> new DBJdbcDebuggerEvaluator(DBJdbcDebugStackFrame.this));

    DBJdbcDebugStackFrame(DBJdbcDebugProcess debugProcess, DebuggerRuntimeInfo runtimeInfo, int index) {
        super(debugProcess, index);
        this.runtimeInfo = runtimeInfo;
    }

    @Override
    protected XSourcePosition resolveSourcePosition() {
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
        return DBDebugSourcePosition.create(virtualFile, lineNumber);
    }

    @Override
    protected VirtualFile resolveVirtualFile() {
        return getDebugProcess().getRuntimeInfoFile(runtimeInfo);
    }

    @NotNull
    @Override
    public DBJdbcDebugValue createDebugValue(String variableName, DBJdbcDebugValue parentValue, List<String> childVariableNames, Icon icon) {
        return new DBJdbcDebugValue(this, parentValue, variableName, childVariableNames, icon);
    }

    @Nullable
    @Override
    protected DBJdbcDebugValue createSuspendReasonDebugValue() {
        return new DBSuspendReasonDebugValue(this);
    }

    @Override
    @NotNull
    public DBJdbcDebuggerEvaluator getEvaluator() {
        return evaluator.get();
    }


    @Nullable
    @Override
    public Object getEqualityObject() {
        DebuggerRuntimeInfo runtimeInfo = getDebugProcess().getRuntimeInfo();
        IdentifierPsiElement subject = getSubject();
        String subjectString = subject == null ? null : subject.getText();
        return runtimeInfo == null ? null : (runtimeInfo.getOwnerName() + "." + runtimeInfo.getProgramName() + "." + subjectString).toLowerCase();
    }
}


