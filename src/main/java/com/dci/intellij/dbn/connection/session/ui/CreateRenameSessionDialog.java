package com.dci.intellij.dbn.connection.session.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.dci.intellij.dbn.language.common.WeakRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CreateRenameSessionDialog extends DBNDialog<CreateRenameSessionForm> {
    private final ConnectionRef connection;
    private WeakRef<DatabaseSession> session;

    public CreateRenameSessionDialog(@NotNull ConnectionHandler connection) {
        super(connection.getProject(), "Create session", true);
        this.connection = connection.ref();
        renameAction(getOKAction(), "Create");
        init();
    }

    public CreateRenameSessionDialog(ConnectionHandler connection, @NotNull DatabaseSession session) {
        super(connection.getProject(), "Rename session", true);
        this.connection = connection.ref();
        this.session = WeakRef.of(session);
        renameAction(getOKAction(), "Rename");
        init();
    }

    @NotNull
    @Override
    protected CreateRenameSessionForm createForm() {
        ConnectionHandler connection = this.connection.ensure();
        return new CreateRenameSessionForm(this, connection, getSession());
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
                    component.getConnection(),
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
