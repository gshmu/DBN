package com.dci.intellij.dbn.connection.console.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CreateRenameConsoleDialog extends DBNDialog<CreateRenameConsoleForm> {
    private ConnectionHandlerRef connectionHandlerRef;
    private DBConsoleVirtualFile console;
    private DBConsoleType consoleType;

    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, @NotNull DBConsoleType consoleType) {
        super(connectionHandler.getProject(), "Create " + consoleType.getName(), true);
        connectionHandlerRef = connectionHandler.getRef();
        this.consoleType = consoleType;
        getOKAction().putValue(Action.NAME, "Create");
        init();
    }

    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, @NotNull DBConsoleVirtualFile console) {
        super(connectionHandler.getProject(), "Rename " + console.getType().getName(), true);
        connectionHandlerRef = connectionHandler.getRef();
        this.console = console;
        getOKAction().putValue(Action.NAME, "Rename");
        init();
    }

    @NotNull
    @Override
    protected CreateRenameConsoleForm createComponent() {
        ConnectionHandler connectionHandler = connectionHandlerRef.getnn();
        return console == null ?
                new CreateRenameConsoleForm(this, connectionHandler, null, consoleType) :
                new CreateRenameConsoleForm(this, connectionHandler, console, console.getType());
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
        CreateRenameConsoleForm component = getComponent();
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

    @Override
    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }
}
