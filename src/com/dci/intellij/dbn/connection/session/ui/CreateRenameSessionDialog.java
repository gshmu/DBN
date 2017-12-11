package com.dci.intellij.dbn.connection.session.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;

public class CreateRenameSessionDialog extends DBNDialog<CreateRenameSessionForm> {
    public CreateRenameSessionDialog(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Create session", true);
        component = new CreateRenameSessionForm(this, connectionHandler, null);
        getOKAction().putValue(Action.NAME, "Create");
        init();
    }

    public CreateRenameSessionDialog(ConnectionHandler connectionHandler, @NotNull DatabaseSession session) {
        super(connectionHandler.getProject(), "Rename session", true);
        component = new CreateRenameSessionForm(this, connectionHandler, session);
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
        DatabaseSessionManager databaseSessionManager = DatabaseSessionManager.getInstance(getProject());
        DatabaseSession session = component.getSession();
        if (session == null) {
            databaseSessionManager.createSession(
                    component.getConnectionHandler(),
                    component.getSessionName());
        } else {
            databaseSessionManager.renameSession(session, component.getSessionName());
        }
        super.doOKAction();
    }

    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }
}
