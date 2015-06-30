package com.dci.intellij.dbn.connection.console.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;

public class CreateRenameConsoleDialog extends DBNDialog<CreateRenameConsoleForm> {
    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, @NotNull DBConsoleType consoleType) {
        super(connectionHandler.getProject(), consoleType == DBConsoleType.DEBUG ? "Create Debug Console" : "Create SQL Console", true);
        component = new CreateRenameConsoleForm(this, connectionHandler, null, consoleType);
        getOKAction().putValue(Action.NAME, "Create");
        init();
    }

    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, @NotNull DBConsoleVirtualFile console) {
        super(connectionHandler.getProject(), "Rename SQL Console", true);
        component = new CreateRenameConsoleForm(this, connectionHandler, console, console.getType());
        getOKAction().putValue(Action.NAME, "Rename");
        init();
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
        DBConsoleVirtualFile console = component.getConsole();
        if (console == null) {
            databaseConsoleManager.createConsole(
                    component.getConnectionHandler(),
                    component.getConsoleName(),
                    component.getConsoleType());
        } else {
            databaseConsoleManager.renameConsole(console, component.getConsoleName());
        }
        super.doOKAction();
    }

    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }
}
