package com.dci.intellij.dbn.connection.session.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CreateRenameSessionDialog extends DBNDialog<CreateRenameSessionForm> {
    private ConnectionHandlerRef connectionHandlerRef;
    private DatabaseSession session;

    public CreateRenameSessionDialog(@NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Create session", true);
        connectionHandlerRef = connectionHandler.getRef();
        getOKAction().putValue(Action.NAME, "Create");
        init();
    }

    public CreateRenameSessionDialog(ConnectionHandler connectionHandler, @NotNull DatabaseSession session) {
        super(connectionHandler.getProject(), "Rename session", true);
        connectionHandlerRef = connectionHandler.getRef();
        this.session = session;
        getOKAction().putValue(Action.NAME, "Rename");
        init();
    }

    @NotNull
    @Override
    protected CreateRenameSessionForm createComponent() {
        ConnectionHandler connectionHandler = connectionHandlerRef.getnn();
        return new CreateRenameSessionForm(this, connectionHandler, session);
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
        CreateRenameSessionForm component = getComponent();
        DatabaseSessionManager databaseSessionManager = DatabaseSessionManager.getInstance(getProject());
        if (session == null) {
            session = databaseSessionManager.createSession(
                    component.getConnectionHandler(),
                    component.getSessionName());
            component.setSession(session);

        } else {
            databaseSessionManager.renameSession(session, component.getSessionName());
        }
        super.doOKAction();
    }

    public DatabaseSession getSession() {
        return session;
    }

    @Override
    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }
}
