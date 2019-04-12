package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DevTestConnectionAction extends AbstractConnectionAction{
    public DevTestConnectionAction(@NotNull ConnectionHandler connectionHandler) {
        super("Dev Test", connectionHandler);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setVisible(DatabaseNavigator.DEVELOPER);
        super.update(e);
    }
}
