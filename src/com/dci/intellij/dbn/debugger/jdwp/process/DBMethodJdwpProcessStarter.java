package com.dci.intellij.dbn.debugger.jdwp.process;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
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
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;

public class DBMethodJdwpProcessStarter extends DBProgramDebugProcessStarter{
    public DBMethodJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @NotNull
    @Override
    public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
        Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();
        RunProfile runProfile = session.getRunProfile();
        if (runProfile == null) {
            throw new ExecutionException("Invalid run profile");
        }

        ExecutionEnvironment environment = ExecutionEnvironmentBuilder.create(session.getProject(), executor, runProfile).build();


        RemoteConnection remoteConnection = new RemoteConnection(true, "localhost", "4000", true);
        RunProfileState state = FailsafeUtil.get(runProfile.getState(executor, environment));

        DebugEnvironment debugEnvironment = new DefaultDebugEnvironment(environment, state, remoteConnection, false);
        DebuggerManagerEx debuggerManagerEx = DebuggerManagerEx.getInstanceEx(session.getProject());
        DebuggerSession debuggerSession = debuggerManagerEx.attachVirtualMachine(debugEnvironment);
        if (debuggerSession == null) {
            throw new ExecutionException("Could not initialize JDWP listener");
        }

        return new DBMethodJdwpDebugProcess(session, debuggerSession, getConnectionHandler());
    }
}
