package com.dci.intellij.dbn.debugger.jdwp.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.jdbc.process.DBProgramDebugProcessStarter;
import com.intellij.debugger.DebugEnvironment;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.DefaultDebugEnvironment;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;

public class DBMethodJdwpProcessStarter extends DBProgramDebugProcessStarter{
    public DBMethodJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @NotNull
    @Override
    public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
        ExecutionEnvironment environment = new ExecutionEnvironment();
        RunProfile runProfile = session.getRunProfile();
        if (runProfile == null) {
            throw new ExecutionException("Invalid run profile");
        }

        Executor executorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
        DebugEnvironment debugEnvironment = new DefaultDebugEnvironment(environment, runProfile.getState(executorInstance, environment), new RemoteConnection(false, "test", "test", true), false);
        DebuggerSession debuggerSession = DebuggerManagerEx.getInstanceEx(session.getProject()).attachVirtualMachine(debugEnvironment);

        return new DBMethodJdwpDebugProcess(session, debuggerSession, getConnectionHandler());
    }
}
