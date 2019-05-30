package com.dci.intellij.dbn.debugger.jdwp.process;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;

public class DBStatementJdwpProcessStarter extends DBJdwpProcessStarter {
    DBStatementJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @Override
    protected DBJdwpDebugProcess createDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession, int tcpPort) {
        return new DBStatementJdwpDebugProcess(session, debuggerSession, getConnectionHandler(), tcpPort);
    }
}
