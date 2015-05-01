package com.dci.intellij.dbn.connection.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;

public class ConnectionAuthenticationDialog extends DBNDialog<ConnectionAuthenticationForm> {
    private boolean rememberCredentials;
    private Authentication authentication;

    public ConnectionAuthenticationDialog(Project project, @Nullable ConnectionHandler connectionHandler, @NotNull Authentication authentication) {
        super(project, "Enter Password", true);
        this.authentication = authentication;
        setModal(true);
        setResizable(false);
        component = new ConnectionAuthenticationForm(this, connectionHandler);
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "Connect");
        okAction.setEnabled(false);
        if (connectionHandler != null) {
            setDoNotAskOption(new DoNotAskOption() {
                @Override
                public boolean isToBeShown() {
                    return true;
                }

                @Override
                public void setToBeShown(boolean toBeShown, int exitCode) {
                    if (exitCode == OK_EXIT_CODE) {
                        rememberCredentials = !toBeShown;
                    }
                }

                @Override
                public boolean canBeHidden() {
                    return true;
                }

                @Override
                public boolean shouldSaveOptionsOnCancel() {
                    return false;
                }

                @NotNull
                @Override
                public String getDoNotShowMessage() {
                    return "Remember credentials";
                }
            });
        }
        init();
    }

    public boolean isRememberCredentials() {
        return rememberCredentials;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void updateConnectButton() {
        getOKAction().setEnabled(authentication.isProvided());
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component == null ? null : component.getPreferredFocusedComponent();
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
