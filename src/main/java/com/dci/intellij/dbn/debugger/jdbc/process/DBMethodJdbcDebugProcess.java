package com.dci.intellij.dbn.debugger.jdbc.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;

public class DBMethodJdbcDebugProcess extends DBJdbcDebugProcess<MethodExecutionInput> {
    DBMethodJdbcDebugProcess(@NotNull XDebugSession session, ConnectionHandler connection) {
        super(session, connection);
    }

    @Override
    protected void executeTarget() throws SQLException {
        MethodExecutionInput methodExecutionInput = getExecutionInput();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(getProject());
        methodExecutionManager.debugExecute(methodExecutionInput, getTargetConnection(), DBDebuggerType.JDBC);
    }

    @Override
    protected boolean isTerminated() {
        return super.isTerminated() || getRuntimeInfo().getOwnerName() == null;
    }

    @Override
    protected void registerDefaultBreakpoint() {
        MethodExecutionInput methodExecutionInput = getExecutionInput();
        DBMethod method = methodExecutionInput.getMethod();
        if (method != null) {
            getBreakpointHandler().registerDefaultBreakpoint(method);
        }

    }

    @Override
    protected void releaseTargetConnection() {
        // method execution processor is responsible for closing
        // the connection after the result is read
        setTargetConnection(null);
    }

    @NotNull
    @Override
    public String getName() {
        DBMethod method = getExecutionInput().getMethod();
        DBSchemaObject object = DBDebugUtil.getMainDatabaseObject(method);
        if (object != null) {
            return object.getQualifiedName();
        }
        return "Debug Process";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        DBMethod method = getExecutionInput().getMethod();
        DBSchemaObject object = DBDebugUtil.getMainDatabaseObject(method);
        if (object != null) {
            return object.getIcon();
        }
        return null;
    }

    @Override
    public ExecutionTarget getExecutionTarget() {
        return ExecutionTarget.METHOD;
    }
}
