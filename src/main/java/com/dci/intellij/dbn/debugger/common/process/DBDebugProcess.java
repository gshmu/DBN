package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.intellij.openapi.project.Project;

public interface DBDebugProcess extends Presentable, PropertyHolder<DBDebugProcessStatus> {
    ConnectionHandler getConnection();

    DBDebugConsoleLogger getConsole();

    Project getProject();

    DatabaseDebuggerInterface getDebuggerInterface();

    ExecutionTarget getExecutionTarget();
}
