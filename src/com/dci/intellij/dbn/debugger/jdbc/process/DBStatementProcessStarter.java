package com.dci.intellij.dbn.debugger.jdbc.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;

public class DBStatementProcessStarter extends DBProgramDebugProcessStarter{
    public DBStatementProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @NotNull
    @Override
    public XDebugProcess start(@NotNull XDebugSession session) {
        return new DBStatementDebugProcess(session, getConnectionHandler());
    }
}
