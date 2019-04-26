package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class ConnectionSelectAction extends DumbAwareAction {
    private final ConnectionHandler connectionHandler;
    private MethodExecutionBrowserForm browserComponent;

    ConnectionSelectAction(MethodExecutionBrowserForm browserComponent, ConnectionHandler connectionHandler) {
        super();
        this.browserComponent = browserComponent;
        this.connectionHandler = connectionHandler;
        getTemplatePresentation().setText(connectionHandler.getName(), false);
        getTemplatePresentation().setIcon(connectionHandler.getIcon());

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        browserComponent.setConnectionHandler(connectionHandler);
    }


}
