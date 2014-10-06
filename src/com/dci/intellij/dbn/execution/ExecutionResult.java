package com.dci.intellij.dbn.execution;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.intellij.openapi.project.Project;

public interface ExecutionResult extends Disposable {

    ExecutionResultForm getResultPanel();

    String getResultName();

    Icon getResultIcon();

    void setExecutionDuration(int executionDuration);
    
    int getExecutionDuration();

    Project getProject();

    ConnectionHandler getConnectionHandler();
}
