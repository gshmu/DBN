package com.dci.intellij.dbn.connection.console;

import com.dci.intellij.dbn.common.util.MessageUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.console.ui.CreateConsoleDialog;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

public class DatabaseConsoleManager extends AbstractProjectComponent {
    private DatabaseConsoleManager(final Project project) {
        super(project);
    }

    public static DatabaseConsoleManager getInstance(Project project) {
        return project.getComponent(DatabaseConsoleManager.class);
    }

    public void showCreateConsoleDialog(final ConnectionHandler connectionHandler) {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                CreateConsoleDialog createConsoleDialog = new CreateConsoleDialog(connectionHandler);
                createConsoleDialog.setModal(true);
                createConsoleDialog.show();
            }
        }.start();
    }

    public void createConsole(ConnectionHandler connectionHandler, String name) {
        DBConsoleVirtualFile console = connectionHandler.getConsole(name, true);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(console, true);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseConsoleManager";
    }

    public void disposeComponent() {
        super.disposeComponent();
    }

    public void deleteConsole(DBConsoleVirtualFile consoleVirtualFile) {
        int exitCode = MessageUtil.showQuestionDialog("You will loose the information contained in this console. Are you sure you want to delete the console?", "Delete Console", MessageUtil.OPTIONS_YES_NO, 0);
        if (exitCode == 0) {
            FileEditorManager.getInstance(getProject()).closeFile(consoleVirtualFile);
            ConnectionHandler connectionHandler = consoleVirtualFile.getConnectionHandler();
            connectionHandler.removeConsole(consoleVirtualFile.getName());
        }

    }
}
