package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JComponent;

public class ConnectionSelectDropdownAction extends DBNComboBoxAction {
    private final MethodExecutionBrowserForm browserComponent;
    private final boolean debug;

    public ConnectionSelectDropdownAction(MethodExecutionBrowserForm browserComponent, boolean debug) {
        this.browserComponent = browserComponent;
        this.debug = debug;
    }

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        Project project = Lookup.getProject(component);
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
/*        for (ConnectionHandler virtualConnectionHandler : connectionBundle.getVirtualConnections()) {
            SelectConnectionAction connectionAction = new SelectConnectionAction(browserComponent, virtualConnectionHandler);
            actionGroup.add(connectionAction);
        }*/

        if (connectionBundle.getConnections().size() > 0) {
            //actionGroup.addSeparator();
            for (ConnectionHandler connection : connectionBundle.getConnections()) {
                if (!debug || DatabaseFeature.DEBUGGING.isSupported(connection)) {
                    ConnectionSelectAction connectionAction = new ConnectionSelectAction(browserComponent, connection);
                    actionGroup.add(connectionAction);
                }
            }
        }

        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        String text = "Select Connection";
        Icon icon = null;

        ConnectionHandler connectionHandler = browserComponent.getSettings().getConnection();
        if (connectionHandler != null) {
            text = connectionHandler.getQualifiedName();
            icon = connectionHandler.getIcon();
        }

        presentation.setText(text, false);
        presentation.setIcon(icon);
    }
 }