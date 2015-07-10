package com.dci.intellij.dbn.debugger.jdwp.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;

public abstract class DBProgramJdwpDebugProcess<T extends ExecutionInput> extends JavaDebugProcess implements Presentable {
    private T executionInput;
    private ConnectionHandlerRef connectionHandlerRef;

    protected DBProgramJdwpDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession, ConnectionHandler connectionHandler) {
        super(session, debuggerSession);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        debuggerManager.registerDebugSession(connectionHandler);

        DBProgramRunConfiguration<T> runProfile = (DBProgramRunConfiguration) session.getRunProfile();
        executionInput = runProfile.getExecutionInput();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    public T getExecutionInput() {
        return executionInput;
    }

    @Override
    public void stop() {
        super.stop();
        Project project = getSession().getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        debuggerManager.unregisterDebugSession(getConnectionHandler());
    }
}
