package com.dci.intellij.dbn.connection.console.ui;

import javax.swing.Action;
import javax.swing.JComponent;

import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;

public class CreateRenameConsoleDialog extends DBNDialog {
    private CreateRenameConsoleForm createConsoleForm;

    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, DBConsoleVirtualFile console) {
        super(connectionHandler.getProject(), console == null ? "Create SQL Console" : "Rename SQL Console", true);
        createConsoleForm = new CreateRenameConsoleForm(this, connectionHandler, console);
        getOKAction().putValue(Action.NAME, console == null ? "Create" : "Rename");
        init();
    }

    protected String getDimensionServiceKey() {
        return null;//"DBNavigator.CreateSQLConsole";
    }      

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getOKAction(),
            getCancelAction()
        };
    }

    @Override
    protected void doOKAction() {
        DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(getProject());
        DBConsoleVirtualFile console = createConsoleForm.getConsole();
        if (console == null) {
            databaseConsoleManager.createConsole(
                    createConsoleForm.getConnectionHandler(),
                    createConsoleForm.getConsoleName());
        } else {
            databaseConsoleManager.renameConsole(console, createConsoleForm.getConsoleName());
        }
        super.doOKAction();
    }

    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }


    @Nullable
    protected JComponent createCenterPanel() {
        return createConsoleForm.getComponent();
    }
}
