package com.dci.intellij.dbn.connection.session.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.dci.intellij.dbn.language.common.WeakRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CreateRenameSessionDialog extends DBNDialog<CreateRenameSessionForm> {
    private final ConnectionHandlerRef connectionHandler;
    private WeakRef<DatabaseSession> session;

    public CreateRenameSessionDialog(@NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Create session", true);
        this.connectionHandler = connectionHandler.getRef();
        getOKAction().putValue(Action.NAME, "Create");
        init();
    }

    public CreateRenameSessionDialog(ConnectionHandler connectionHandler, @NotNull DatabaseSession session) {
        super(connectionHandler.getProject(), "Rename session", true);
        this.connectionHandler = connectionHandler.getRef();
        this.session = WeakRef.of(session);
        getOKAction().putValue(Action.NAME, "Rename");
        init();
    }

    @NotNull
    @Override
    protected CreateRenameSessionForm createForm() {
        ConnectionHandler connectionHandler = this.connectionHandler.ensure();
        return new CreateRenameSessionForm(this, connectionHandler, getSession());
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
        CreateRenameSessionForm component = getForm();
        DatabaseSessionManager databaseSessionManager = DatabaseSessionManager.getInstance(getProject());
        if (session == null) {
            DatabaseSession session = databaseSessionManager.createSession(
                    component.getConnectionHandler(),
                    component.getSessionName());
            this.session = WeakRef.of(session);
            component.setSession(session);

        } else {
            databaseSessionManager.renameSession(getSession(), component.getSessionName());
        }
        super.doOKAction();
    }

    public DatabaseSession getSession() {
        return WeakRef.get(session);
    }

    @Override
    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }
}
