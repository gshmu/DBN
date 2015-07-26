package com.dci.intellij.dbn.debugger.jdwp.process;

import javax.swing.Icon;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugProcess;
import com.dci.intellij.dbn.execution.ExecutionType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.xdebugger.XDebugSession;

public class DBMethodJdwpDebugProcess extends DBJdwpDebugProcess<MethodExecutionInput> {
    public DBMethodJdwpDebugProcess(@NotNull XDebugSession session, @NotNull DebuggerSession debuggerSession, ConnectionHandler connectionHandler) {
        super(session, debuggerSession, connectionHandler);
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
    public String getDescription() {
        return null;
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

    @Nullable
    protected DBSchemaObject getMainDatabaseObject(DBMethod method) {
        return method != null && method.isProgramMethod() ? method.getProgram() : method;
    }

    @Override
    protected void executeTarget() throws SQLException {
        MethodExecutionInput methodExecutionInput = getExecutionInput();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(getProject());
        methodExecutionManager.debugExecute(methodExecutionInput, getTargetConnection(), ExecutionType.DEBUG_JWDP);
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
        targetConnection = null;
    }
}
