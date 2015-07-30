package com.dci.intellij.dbn.debugger.jdwp.process;

import java.net.ServerSocket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.dci.intellij.dbn.debugger.jdwp.config.DBMethodJdwpRunConfig;
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
import com.intellij.util.Range;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;

public class DBMethodJdwpProcessStarter extends DBDebugProcessStarter {
    public DBMethodJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @NotNull
    @Override
    public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
        Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();
        RunProfile runProfile = session.getRunProfile();
        assertNotNull(runProfile, "Invalid run profile");

        ExecutionEnvironment environment = ExecutionEnvironmentBuilder.create(session.getProject(), executor, runProfile).build();
        DBMethodJdwpRunConfig methodRunConfig = (DBMethodJdwpRunConfig) runProfile;
        Range<Integer> portRange = methodRunConfig.getTcpPortRange();
        int freePort = findFreePort(portRange.getFrom(), portRange.getTo());
        RemoteConnection remoteConnection = new RemoteConnection(true, "localhost", Integer.toString(freePort), true);
        RunProfileState state = FailsafeUtil.get(runProfile.getState(executor, environment));

        DebugEnvironment debugEnvironment = new DefaultDebugEnvironment(environment, state, remoteConnection, false);
        DebuggerManagerEx debuggerManagerEx = DebuggerManagerEx.getInstanceEx(session.getProject());
        DebuggerSession debuggerSession = debuggerManagerEx.attachVirtualMachine(debugEnvironment);
        assertNotNull(debuggerSession, "Could not initialize JDWP listener");

        return new DBMethodJdwpDebugProcess(session, debuggerSession, getConnectionHandler());
    }

    private @NotNull <T> T assertNotNull(@Nullable T object, String message) throws ExecutionException {
        if (object == null) {
            throw new ExecutionException(message);
        }
        return object;
    }

    private static int findFreePort(int minPortNumber, int maxPortNumber) throws ExecutionException {
        for (int portNumber = minPortNumber; portNumber < maxPortNumber; portNumber++) {
            try {
                ServerSocket socket = null;
                try {
                    socket = new ServerSocket(portNumber);
                    return portNumber;
                } finally {
                    if (socket != null) {
                        socket.close();
                    }

                }
            } catch (Exception ignore) {}
        }
        throw new ExecutionException("Could not find free port in the range " + minPortNumber + " - " + maxPortNumber);
    }
}
