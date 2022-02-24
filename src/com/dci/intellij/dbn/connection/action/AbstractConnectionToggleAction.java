package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public abstract class AbstractConnectionToggleAction extends ToggleAction {
    private final ConnectionHandlerRef connection;

    public AbstractConnectionToggleAction(String text, @NotNull ConnectionHandler connection) {
        this(text, null, connection);

    }
    public AbstractConnectionToggleAction(String text, Icon icon, @NotNull ConnectionHandler connection) {
        this(text, null, icon, connection);
    }
    public AbstractConnectionToggleAction(String text, String description, Icon icon, @NotNull ConnectionHandler connection) {
        super(text, description, icon);
        this.connection = connection.ref();
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return connection.ensure();
    }
}
