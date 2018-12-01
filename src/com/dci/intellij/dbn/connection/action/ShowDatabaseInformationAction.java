package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowDatabaseInformationAction extends AbstractConnectionAction {

    public ShowDatabaseInformationAction(ConnectionHandler connectionHandler) {
        super("Connection Info", connectionHandler);
        //getTemplatePresentation().setEnabled(connectionHandler.getConnectionStatus().isConnected());
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        TaskInstructions taskInstructions = new TaskInstructions("Loading database information for " + connectionHandler.getName(), TaskInstruction.MANAGED);
        ConnectionAction.invoke("showing database information", connectionHandler, taskInstructions, action -> {
            FailsafeUtil.check(connectionHandler);
            ConnectionManager.showConnectionInfoDialog(connectionHandler);
        });
    }
}
