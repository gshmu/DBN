package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OpenSQLConsoleAction extends DumbAwareAction {
    public OpenSQLConsoleAction() {
        super("Open SQL Console", null, Icons.FILE_SQL_CONSOLE);
    }

    private ConnectionHandler getConnectionHandler(@NotNull AnActionEvent e) {
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

    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler(e);

        if (connectionHandler != null) {
            DefaultActionGroup actionGroup = new DefaultActionGroup();
            Collection<DBConsoleVirtualFile> consoles = connectionHandler.getConsoles();
            for (DBConsoleVirtualFile console : consoles) {
                actionGroup.add(new SelectConsoleAction(console));
            }
            actionGroup.add(Separator.getInstance());
            actionGroup.add(new SelectConsoleAction(connectionHandler));

            ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Open SQL Console",
                    actionGroup,
                    e.getDataContext(),
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true, null, 10);

            //Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
            Component component = (Component) e.getInputEvent().getSource();
            showBelowComponent(popup, component);
        }
    }

    private static void showBelowComponent(ListPopup popup, Component component) {
        Point locationOnScreen = component.getLocationOnScreen();
        Point location = new Point(
                (int) (locationOnScreen.getX()),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }


    private class SelectConsoleAction extends AnAction{
        private ConnectionHandler connectionHandler;
        private DBConsoleVirtualFile consoleVirtualFile;

        public SelectConsoleAction(ConnectionHandler connectionHandler) {
            super("Create new...");
            this.connectionHandler = connectionHandler;
        }

        public SelectConsoleAction(DBConsoleVirtualFile consoleVirtualFile) {
            super(consoleVirtualFile.getName(), null, consoleVirtualFile.getIcon());
            this.consoleVirtualFile = consoleVirtualFile;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (consoleVirtualFile == null) {
                DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(connectionHandler.getProject());
                databaseConsoleManager.showCreateConsoleDialog(connectionHandler);
            } else {
                ConnectionHandler connectionHandler = consoleVirtualFile.getConnectionHandler();
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
                fileEditorManager.openFile(consoleVirtualFile, true);
            }
        }
    }
}
