package com.dci.intellij.dbn.connection.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.project.Project;

public class ConnectionPasswordDialog extends DBNDialog<ConnectionPasswordForm> {
    private boolean rememberPassword;
    private String password;

    public ConnectionPasswordDialog(Project project, @Nullable ConnectionHandler connectionHandler) {
        super(project, "Enter Password", true);
        setModal(true);
        setResizable(false);
        component = new ConnectionPasswordForm(this, connectionHandler);
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "Connect");
        okAction.setEnabled(false);
        init();
    }

    public boolean isRememberPassword() {
        return rememberPassword;
    }

    public void setRememberPassword(boolean rememberPassword) {
        this.rememberPassword = rememberPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        getOKAction().setEnabled(StringUtil.isNotEmpty(password));
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
