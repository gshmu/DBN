package com.dci.intellij.dbn.debugger.jdwp.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.xdebugger.XDebugSession;

public class DBMethodJdwpProcessStarter extends DBJdwpProcessStarter {
    public DBMethodJdwpProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @NotNull
    protected DBMethodJdwpDebugProcess createDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession) {
        return new DBMethodJdwpDebugProcess(session, debuggerSession, getConnectionHandler());
    }

}
