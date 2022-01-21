package com.dci.intellij.dbn.connection.console.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;

public class CreateRenameConsoleDialog extends DBNDialog<CreateRenameConsoleForm> {
    private ConnectionHandlerRef connectionHandlerRef;
    private DBConsole console;
    private DBConsoleType consoleType;

    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, @NotNull DBConsoleType consoleType) {
        super(connectionHandler.getProject(), "Create " + consoleType.getName(), true);
        connectionHandlerRef = connectionHandler.getRef();
        this.consoleType = consoleType;
        renameAction(getOKAction(), "Create");
        init();
    }

    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, @NotNull DBConsole console) {
        super(connectionHandler.getProject(), "Rename " + console.getConsoleType().getName(), true);
        connectionHandlerRef = connectionHandler.getRef();
        this.console = console;
        renameAction(getOKAction(), "Rename");
        init();
    }

    @NotNull
    @Override
    protected CreateRenameConsoleForm createForm() {
        ConnectionHandler connectionHandler = connectionHandlerRef.ensure();
        return console == null ?
                new CreateRenameConsoleForm(this, connectionHandler, null, consoleType) :
                new CreateRenameConsoleForm(this, connectionHandler, console, console.getConsoleType());
    }

    @Override
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
        CreateRenameConsoleForm component = getForm();
        DBConsole console = component.getConsole();
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

    @Override
    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }
}
