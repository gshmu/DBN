package com.dci.intellij.dbn.connection.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.jdi.JDIProcessListener;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class DevTestConnectionAction extends AbstractConnectionAction{
    public DevTestConnectionAction(@NotNull ConnectionHandler connectionHandler) {
        super("Dev Test", connectionHandler);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        JDIProcessListener.start(getConnectionHandler());
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setVisible(DatabaseNavigator.getInstance().isDeveloperModeEnabled());
        super.update(e);
    }
}
