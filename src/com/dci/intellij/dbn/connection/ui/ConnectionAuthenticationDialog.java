package com.dci.intellij.dbn.connection.ui;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;

public class ConnectionAuthenticationDialog extends DBNDialog<ConnectionAuthenticationForm> {
    private boolean rememberCredentials;
    private final WeakRef<AuthenticationInfo> authenticationInfo; // TODO dialog result - Disposable.nullify(...)
    private final ConnectionHandlerRef connection;

    public ConnectionAuthenticationDialog(Project project, @Nullable ConnectionHandler connection, @NotNull AuthenticationInfo authenticationInfo) {
        super(project, "Enter password", true);
        this.authenticationInfo = WeakRef.of(authenticationInfo);
        setModal(true);
        setResizable(false);
        this.connection = ConnectionHandlerRef.of(connection);
        Action okAction = getOKAction();
        renameAction(okAction, "Connect");
        okAction.setEnabled(false);
        if (connection != null) {
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

    @NotNull
    @Override
    protected ConnectionAuthenticationForm createForm() {
        ConnectionHandler connection = ConnectionHandlerRef.get(this.connection);
        return new ConnectionAuthenticationForm(this, connection);
    }

    public boolean isRememberCredentials() {
        return rememberCredentials;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return WeakRef.get(authenticationInfo);
    }

    public void updateConnectButton() {
        getOKAction().setEnabled(getAuthenticationInfo().isProvided());
    }

    @Override
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
}
