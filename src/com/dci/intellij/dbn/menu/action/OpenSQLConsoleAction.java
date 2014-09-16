package com.dci.intellij.dbn.menu.action;

import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;

public class OpenSQLConsoleAction extends DumbAwareAction {
    private ConnectionHandler latestSelection; // todo move to data context

    public OpenSQLConsoleAction() {
        super("Open SQL console...", null, Icons.FILE_SQL_CONSOLE);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            List<ConnectionBundle> connectionBundles = connectionManager.getConnectionBundles();


            ConnectionHandler singleConnectionHandler = null;
            DefaultActionGroup actionGroup = new DefaultActionGroup();
            for (ConnectionBundle connectionBundle : connectionBundles) {
                if (connectionBundle.getConnectionHandlers().size() > 0) {
                    actionGroup.addSeparator();
                    for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                        SelectConnectionAction connectionAction = new SelectConnectionAction(connectionHandler);
                        actionGroup.add(connectionAction);
                        singleConnectionHandler = connectionHandler;
                    }
                }
            }

            if (actionGroup.getChildrenCount() > 1) {
                ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                        "Select console connection",
                        actionGroup,
                        e.getDataContext(),
                        //JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true,
                        true,
                        true,
                        null,
                        actionGroup.getChildrenCount(),
                        new Condition<AnAction>() {
                            @Override
                            public boolean value(AnAction action) {
                                SelectConnectionAction selectConnectionAction = (SelectConnectionAction) action;
                                return latestSelection == selectConnectionAction.connectionHandler;
                            }
                        });

                popupBuilder.showCenteredInCurrentWindow(project);
            } else {
                if (singleConnectionHandler != null) {
                    openSQLConsole(singleConnectionHandler);
                } else {
                    int selection = MessageUtil.showInfoDialog(
                            "No database connections found. Please setup a connection first",
                            "No connections available.", new String[]{"Setup Connection", "Cancel"}, 0);
                    if (selection == 0) {
                        GlobalProjectSettingsDialog globalSettingsDialog = new GlobalProjectSettingsDialog(project);
                        globalSettingsDialog.show();
                    }
                }

            }
        }

    }

    private class SelectConnectionAction extends DumbAwareAction{
        private ConnectionHandler connectionHandler;

        private SelectConnectionAction(ConnectionHandler connectionHandler) {
            super(connectionHandler.getName(), null, connectionHandler.getIcon());
            this.connectionHandler = connectionHandler;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            openSQLConsole(connectionHandler);
            latestSelection = connectionHandler;
        }
    }

    private void openSQLConsole(ConnectionHandler connectionHandler) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(connectionHandler.getSQLConsoleFile(), true);
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = ActionUtil.getProject(e);
        presentation.setEnabled(project != null);
        presentation.setIcon(Icons.FILE_SQL_CONSOLE);
    }
}
