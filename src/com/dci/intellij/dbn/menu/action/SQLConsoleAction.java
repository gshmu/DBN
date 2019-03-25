package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;

public class SQLConsoleAction extends DumbAwareAction {
    public SQLConsoleAction() {
        super("Open SQL console...", null, Icons.FILE_SQL_CONSOLE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        Project project = ActionUtil.ensureProject(e);
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();


        ConnectionHandler singleConnectionHandler = null;
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connectionBundle.getConnectionHandlers().size() > 0) {
            actionGroup.addSeparator();
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                SelectConnectionAction connectionAction = new SelectConnectionAction(connectionHandler);
                actionGroup.add(connectionAction);
                singleConnectionHandler = connectionHandler;
            }
        }

        if (actionGroup.getChildrenCount() > 1) {
            ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Select Console Connection",
                    actionGroup,
                    e.getDataContext(),
                    //JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    false,
                    true,
                    true,
                    null,
                    actionGroup.getChildrenCount(),
                    preselect -> {
/*
                        SelectConsoleAction selectConnectionAction = (SelectConsoleAction) action;
                        return latestSelection == selectConnectionAction.connectionHandler;
*/
                        return true;
                    });

            popupBuilder.showCenteredInCurrentWindow(project);
        } else {
            if (singleConnectionHandler != null) {
                openSQLConsole(singleConnectionHandler);
            } else {
                MessageUtil.showInfoDialog(
                        project, "No connections available.", "No database connections found. Please setup a connection first",
                        new String[]{"Setup Connection", "Cancel"}, 0,
                        (option) -> conditional(option == 0,
                                () -> {
                                    ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                                    settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
                                }));
            }

        }

    }

    private class SelectConnectionAction extends ActionGroup {
        private ConnectionHandlerRef connectionHandlerRef;

        private SelectConnectionAction(ConnectionHandler connectionHandler) {
            super(connectionHandler.getName(), null, connectionHandler.getIcon());
            this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
            setPopup(true);
        }
/*
        @Override
        public void actionPerformed(AnActionEvent e) {
            openSQLConsole(connectionHandler);
            latestSelection = connectionHandler;
        }*/

        @NotNull
        @Override
        public AnAction[] getChildren(AnActionEvent e) {
            ConnectionHandler connectionHandler = connectionHandlerRef.ensure();
            List<AnAction> actions = new ArrayList<>();
            Collection<DBConsoleVirtualFile> consoles = connectionHandler.getConsoleBundle().getConsoles();
            for (DBConsoleVirtualFile console : consoles) {
                actions.add(new SelectConsoleAction(console));
            }
            actions.add(Separator.getInstance());
            actions.add(new SelectConsoleAction(connectionHandler, DBConsoleType.STANDARD));
            if (DatabaseFeature.DEBUGGING.isSupported(connectionHandler)) {
                actions.add(new SelectConsoleAction(connectionHandler, DBConsoleType.DEBUG));
            }

            return actions.toArray(new AnAction[0]);
        }
    }

    private class SelectConsoleAction extends AnAction{
        private ConnectionHandlerRef connectionHandlerRef;
        private DBConsoleVirtualFile consoleVirtualFile;
        private DBConsoleType consoleType;


        SelectConsoleAction(ConnectionHandler connectionHandler, DBConsoleType consoleType) {
            super("New " + consoleType.getName() + "...");
            this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
            this.consoleType = consoleType;
        }

        SelectConsoleAction(DBConsoleVirtualFile consoleVirtualFile) {
            super(consoleVirtualFile.getName(), null, consoleVirtualFile.getIcon());
            this.consoleVirtualFile = consoleVirtualFile;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (consoleVirtualFile == null) {
                ConnectionHandler connectionHandler = connectionHandlerRef.ensure();
                DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(connectionHandler.getProject());
                databaseConsoleManager.showCreateConsoleDialog(connectionHandler, consoleType);
            } else {
                ConnectionHandler connectionHandler = Failsafe.get(consoleVirtualFile.getConnectionHandler());
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
                fileEditorManager.openFile(consoleVirtualFile, true);
            }
        }
    }

    private static void openSQLConsole(ConnectionHandler connectionHandler) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(connectionHandler.getConsoleBundle().getDefaultConsole(), true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = ActionUtil.getProject(e);
        presentation.setEnabled(project != null);
        presentation.setIcon(Icons.FILE_SQL_CONSOLE);
    }
}
