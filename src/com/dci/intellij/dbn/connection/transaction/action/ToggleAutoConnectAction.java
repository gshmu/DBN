package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionToggleAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ToggleAutoConnectAction extends AbstractConnectionToggleAction {

    public ToggleAutoConnectAction(ConnectionHandler connectionHandler) {
        super("Connect Automatically", connectionHandler);

    }
    @Override
    public boolean isSelected(AnActionEvent e) {
        return getConnectionHandler().getSettings().getDetailSettings().isConnectAutomatically();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        getConnectionHandler().getSettings().getDetailSettings().setConnectAutomatically(state);
    }
}
