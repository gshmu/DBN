package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class SelectConnectionAction extends DumbAwareAction {
    private final ConnectionHandler connectionHandler;
    private MethodExecutionBrowserForm browserComponent;

    public SelectConnectionAction(MethodExecutionBrowserForm browserComponent, ConnectionHandler connectionHandler) {
        super();
        this.browserComponent = browserComponent;
        this.connectionHandler = connectionHandler;
        getTemplatePresentation().setText(connectionHandler.getName(), false);
        getTemplatePresentation().setIcon(connectionHandler.getIcon());

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        browserComponent.setConnectionHandler(connectionHandler);
    }


}
