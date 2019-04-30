package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.action.AbstractConnectionToggleAction;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class HidePseudoColumnsToggleAction extends AbstractConnectionToggleAction {

    HidePseudoColumnsToggleAction(ConnectionHandler connectionHandler) {
        super("Hide pseudo columns", connectionHandler);

    }
    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler.getSettings().getFilterSettings().isHidePseudoColumns();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.getSettings().getFilterSettings().setHidePseudoColumns(state);
        ConnectionId connectionId = connectionHandler.getConnectionId();
        EventUtil.notify(
                connectionHandler.getProject(),
                ObjectFilterChangeListener.TOPIC,
                (listener) -> listener.nameFiltersChanged(connectionId, DBObjectType.COLUMN));

    }
}
