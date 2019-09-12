package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SQLConsoleOpenAction extends GroupPopupAction {
    public SQLConsoleOpenAction() {
        super("Open SQL Console", "SQL Console", Icons.FILE_SQL_CONSOLE);
    }

    private static ConnectionHandler getConnectionHandler(@NotNull AnActionEvent e) {
        Project project = Lookup.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getActiveConnection();
        }
        return null;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        presentation.setEnabled(connectionHandler != null);
        presentation.setText("Open SQL Console");
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        List<AnAction> actions = new ArrayList<>();
        if (connectionHandler != null) {
            Collection<DBConsole> consoles = connectionHandler.getConsoleBundle().getConsoles();
            for (DBConsole console : consoles) {
                actions.add(new SelectConsoleAction(console));
            }
            actions.add(Separator.getInstance());
            actions.add(new SelectConsoleAction(connectionHandler, DBConsoleType.STANDARD));
            if (DatabaseFeature.DEBUGGING.isSupported(connectionHandler)) {
                actions.add(new SelectConsoleAction(connectionHandler, DBConsoleType.DEBUG));
            }
        }
        return actions.toArray(new AnAction[0]);
    }


    private static class SelectConsoleAction extends AbstractConnectionAction{
        private DBConsole console;
        private DBConsoleType consoleType;

        SelectConsoleAction(@NotNull ConnectionHandler connectionHandler, @NotNull DBConsoleType consoleType) {
            super("New " + consoleType.getName() + "...", connectionHandler);
            this.consoleType = consoleType;
        }

        SelectConsoleAction(DBConsole console) {
            super(console.getName().replaceAll("_", "__"), null, console.getIcon(), console.getConnectionHandler());
            this.console = console;
        }

        @Override
        protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
            if (console == null) {
                DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(project);
                databaseConsoleManager.showCreateConsoleDialog(connectionHandler, consoleType);
            } else {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                fileEditorManager.openFile(console.getVirtualFile(), true);
            }
        }
    }
}
