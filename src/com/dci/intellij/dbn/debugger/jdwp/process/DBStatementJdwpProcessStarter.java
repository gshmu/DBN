package com.dci.intellij.dbn.debugger.jdwp.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.xdebugger.XDebugSession;

public class DBStatementJdwpProcessStarter extends DBJdwpProcessStarter {
    public DBStatementJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @Override
    protected DBJdwpDebugProcess createDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession, int tcpPort) {
        return new DBStatementJdwpDebugProcess(session, debuggerSession, getConnectionHandler(), tcpPort);
    }
}
