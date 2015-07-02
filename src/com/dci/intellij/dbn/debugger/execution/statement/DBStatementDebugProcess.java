package com.dci.intellij.dbn.debugger.execution.statement;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;

public class DBStatementDebugProcess extends DBProgramDebugProcess<StatementExecutionInput>{
    public DBStatementDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session, connectionHandler);
    }

    @Override
    protected void doExecuteTarget() throws SQLException {
        StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(getProject());
        statementExecutionManager.debugExecute(getExecutionProcessor(), getTargetConnection());

    }

    @Override
    protected void registerDefaultBreakpoint() {
        DBStatementRunConfiguration runConfiguration = (DBStatementRunConfiguration) getSession().getRunProfile();
        if (runConfiguration != null) {
            List<DBMethod> methods = runConfiguration.getMethods();
            if (methods.size() > 0) {
                registerDefaultBreakpoint(methods.get(0));
            }
        }
/*
        try {
            defaultBreakpointInfo = getDebuggerInterface().addSourceBreakpoint(1, getDebugConnection());
        } catch (SQLException e) {
            NotificationUtil.sendErrorNotification(getProject(), "Error setting breakpoint.", e.getMessage());
        }
*/
    }

    public VirtualFile getRuntimeInfoFile(DebuggerRuntimeInfo runtimeInfo) {
        DBSchemaObject schemaObject = getDatabaseObject(runtimeInfo);
        return schemaObject == null ?
            getExecutionProcessor().getVirtualFile() :
            schemaObject.getVirtualFile();
    }
    private StatementExecutionProcessor getExecutionProcessor() {
        return getExecutionInput().getExecutionProcessor();
    }

    @NotNull
    @Override
    public String getName() {
        return getExecutionProcessor().getPsiFile().getName();
    }
    @Nullable
    @Override
    public Icon getIcon() {
        return getExecutionProcessor().getPsiFile().getIcon();
    }
}
