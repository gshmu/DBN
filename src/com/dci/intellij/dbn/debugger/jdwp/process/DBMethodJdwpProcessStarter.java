package com.dci.intellij.dbn.debugger.jdwp.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;

public class DBMethodJdwpProcessStarter extends DBJdwpProcessStarter {
    DBMethodJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @Override
    @NotNull
    protected DBMethodJdwpDebugProcess createDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession, int tcpPort) {
        return new DBMethodJdwpDebugProcess(session, debuggerSession, getConnectionHandler(), tcpPort);
    }

}
