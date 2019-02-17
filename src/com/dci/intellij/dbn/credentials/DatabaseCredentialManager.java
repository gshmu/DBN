package com.dci.intellij.dbn.credentials;

import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseCredentialManager implements BaseComponent {
    public static boolean USE = false;

    public static DatabaseCredentialManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseCredentialManager.class);
    }


    public void removePassword(ConnectionId connectionId, String userName) {
        setPassword(connectionId, userName, null);
    }

    public void setPassword(ConnectionId connectionId, String userName, @Nullable String password) {
        String serviceName = getConnectionServiceName(connectionId);
        CredentialAttributes credentialAttributes = new CredentialAttributes(serviceName, userName, this.getClass(), false);
        PasswordSafe.getInstance().setPassword(credentialAttributes, password);
    }

    @Nullable
    public String getPassword(ConnectionId connectionId, String userName) {
        String serviceName = getConnectionServiceName(connectionId);
        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        CredentialAttributes credentialAttributes = new CredentialAttributes(serviceName, userName, this.getClass(), false);
        return passwordSafe.getPassword(credentialAttributes);
    }

    private boolean isMemoryStorage() {
        return PasswordSafe.getInstance().isMemoryOnly();
    }

    @NotNull
    protected String getConnectionServiceName(ConnectionId connectionId) {
        return "DBNavigator.Connection." + connectionId;
    }

    @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() { }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.DatabaseCredentialManager";
    }
}
