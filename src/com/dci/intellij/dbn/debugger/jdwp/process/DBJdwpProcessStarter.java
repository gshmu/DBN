package com.dci.intellij.dbn.debugger.jdwp.process;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.dci.intellij.dbn.debugger.jdwp.config.DBJdwpRunConfig;
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
import com.intellij.openapi.util.Key;
import com.intellij.util.Range;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.ServerSocket;

public abstract class DBJdwpProcessStarter extends DBDebugProcessStarter {

    public static final Key<Integer> JDWP_DEBUGGER_PORT = new Key<Integer>("JDWP_DEBUGGER_PORT");

    public DBJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
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

    @NotNull
    @Override
    public final XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
        Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();
        RunProfile runProfile = session.getRunProfile();
        assertNotNull(runProfile, "Invalid run profile");
        if (runProfile instanceof DBRunConfig) {
            DBRunConfig runConfig = (DBRunConfig) runProfile;
            runConfig.setCanRun(true);
        }

        ExecutionEnvironment environment = ExecutionEnvironmentBuilder.create(session.getProject(), executor, runProfile).build();
        DBJdwpRunConfig jdwpRunConfig = (DBJdwpRunConfig) runProfile;
        Range<Integer> portRange = jdwpRunConfig.getTcpPortRange();
        int freePort = findFreePort(portRange.getFrom(), portRange.getTo());
        RemoteConnection remoteConnection = new RemoteConnection(true, "localhost", Integer.toString(freePort), true);
        RunProfileState state = Failsafe.nn(runProfile.getState(executor, environment));

        DebugEnvironment debugEnvironment = new DefaultDebugEnvironment(environment, state, remoteConnection, true);
        DebuggerManagerEx debuggerManagerEx = DebuggerManagerEx.getInstanceEx(session.getProject());
        DebuggerSession debuggerSession = debuggerManagerEx.attachVirtualMachine(debugEnvironment);
        assertNotNull(debuggerSession, "Could not initialize JDWP listener");

        return createDebugProcess(session, debuggerSession, freePort);
    }

    protected abstract DBJdwpDebugProcess createDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession, int tcpPort);

    private @NotNull <T> T assertNotNull(@Nullable T object, String message) throws ExecutionException {
        if (object == null) {
            throw new ExecutionException(message);
        }
        return object;
    }
}
