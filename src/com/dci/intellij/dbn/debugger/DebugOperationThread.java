package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.intellij.openapi.project.Project;

import java.sql.SQLException;

public abstract class DebugOperationThread extends Thread {
    private Project project;
    private String operationName;

    protected DebugOperationThread(Project project, String operationName) {
        super("DBN Debug Operation (" + operationName + ')');
        this.project = project;
        this.operationName = operationName;
    }

    @Override
    public final void run() {
        try {
            executeOperation();
        } catch (final SQLException e) {
            NotificationUtil.sendErrorNotification(project, "Error performing debug operation (" + operationName + ").", e.getMessage());
            //MessageUtil.showErrorDialog(getProject(), "Could not perform debug operation (" + operationName + ").", e);
        }
    }

    public abstract void executeOperation() throws SQLException;
}
