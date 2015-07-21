package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.intellij.openapi.project.Project;

public interface DBDebugProcess extends Presentable {
    DBDebugProcessStatus getStatus();

    ConnectionHandler getConnectionHandler();

    DBDebugConsoleLogger getConsole();

    Project getProject();

    DatabaseDebuggerInterface getDebuggerInterface();
}
