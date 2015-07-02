package com.dci.intellij.dbn.browser.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

public class OpenSQLConsoleAction extends GroupPopupAction {
    public OpenSQLConsoleAction() {
        super("Open SQL Console", "SQL Console", Icons.FILE_SQL_CONSOLE);
    }

    private static ConnectionHandler getConnectionHandler(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getActiveConnection();
        }
        return null;
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        presentation.setEnabled(connectionHandler != null);
        presentation.setText("Open SQL Console");
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        List<AnAction> actions = new ArrayList<AnAction>();
        if (connectionHandler != null) {
            Collection<DBConsoleVirtualFile> consoles = connectionHandler.getConsoleBundle().getConsoles();
            for (DBConsoleVirtualFile console : consoles) {
                actions.add(new SelectConsoleAction(console));
            }
            actions.add(Separator.getInstance());
            actions.add(new SelectConsoleAction(connectionHandler, DBConsoleType.STANDARD));
            if (DatabaseFeature.DEBUGGING.isSupported(connectionHandler)) {
                actions.add(new SelectConsoleAction(connectionHandler, DBConsoleType.DEBUG));
            }
        }
        return actions.toArray(new AnAction[actions.size()]);
    }


    private class SelectConsoleAction extends AbstractConnectionAction{
        private DBConsoleVirtualFile consoleVirtualFile;
        private DBConsoleType consoleType;

        public SelectConsoleAction(ConnectionHandler connectionHandler, @NotNull DBConsoleType consoleType) {
            super("New " + consoleType.getName() + "...", connectionHandler);
            this.consoleType = consoleType;
        }

        public SelectConsoleAction(DBConsoleVirtualFile consoleVirtualFile) {
            super(consoleVirtualFile.getName(), null, consoleVirtualFile.getIcon(), consoleVirtualFile.getConnectionHandler());
            this.consoleVirtualFile = consoleVirtualFile;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (consoleVirtualFile == null) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(connectionHandler.getProject());
                databaseConsoleManager.showCreateConsoleDialog(connectionHandler, consoleType);
            } else {
                ConnectionHandler connectionHandler = consoleVirtualFile.getConnectionHandler();
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
                fileEditorManager.openFile(consoleVirtualFile, true);
            }
        }
    }
}
