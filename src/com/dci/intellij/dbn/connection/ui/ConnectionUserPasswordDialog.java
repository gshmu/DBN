package com.dci.intellij.dbn.connection.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.project.Project;

public class ConnectionUserPasswordDialog extends DBNDialog<ConnectionUserPasswordForm> {
    private boolean rememberUser;
    private boolean rememberPassword;
    private Authentication authentication = new Authentication();

    public ConnectionUserPasswordDialog(Project project, @Nullable ConnectionHandler connectionHandler) {
        super(project, "Enter Password", true);
        setModal(true);
        setResizable(false);
        component = new ConnectionUserPasswordForm(this, connectionHandler);
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "Connect");
        okAction.setEnabled(false);
        init();
    }

    public boolean isRememberUser() {
        return rememberUser;
    }

    public void setRememberUser(boolean rememberUser) {
        this.rememberUser = rememberUser;
    }

    public boolean isRememberPassword() {
        return rememberPassword;
    }

    public void setRememberPassword(boolean rememberPassword) {
        this.rememberPassword = rememberPassword;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void updateConnectButton() {
        getOKAction().setEnabled(authentication.isProvided());
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
        };
    }
    
    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
