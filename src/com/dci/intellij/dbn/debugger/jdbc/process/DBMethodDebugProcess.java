package com.dci.intellij.dbn.debugger.jdbc.process;

import javax.swing.Icon;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.xdebugger.XDebugSession;

public class DBMethodDebugProcess extends DBProgramDebugProcess<MethodExecutionInput>{
    public DBMethodDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session, connectionHandler);
    }

    @Override
    protected void doExecuteTarget() throws SQLException {
        MethodExecutionInput methodExecutionInput = getExecutionInput();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(getProject());
        methodExecutionManager.debugExecute(methodExecutionInput, getTargetConnection());
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
            registerDefaultBreakpoint(method);
        }

    }

    @Override
    protected void releaseTargetConnection() {
        // method execution processor is responsible for closing
        // the connection after the result is read
        targetConnection = null;
    }

    @NotNull
    @Override
    public String getName() {
        DBMethod method = getExecutionInput().getMethod();
        DBSchemaObject object = getMainDatabaseObject(method);
        if (object != null) {
            return object.getQualifiedName();
        }
        return "Debug Process";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        DBMethod method = getExecutionInput().getMethod();
        DBSchemaObject object = getMainDatabaseObject(method);
        if (object != null) {
            return object.getIcon();
        }
        return null;
    }
}
