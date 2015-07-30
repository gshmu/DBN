package com.dci.intellij.dbn.debugger.jdbc.process;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStarter;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;

public class DBStatementJdbcProcessStarter extends DBDebugProcessStarter {
    public DBStatementJdbcProcessStarter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @NotNull
    @Override
    public XDebugProcess start(@NotNull XDebugSession session) {
        return new DBStatementJdbcDebugProcess(session, getConnectionHandler());
    }
}
