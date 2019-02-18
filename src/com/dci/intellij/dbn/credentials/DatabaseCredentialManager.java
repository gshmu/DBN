package com.dci.intellij.dbn.credentials;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
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


    public void removePassword(@NotNull ConnectionId connectionId, @NotNull String userName) {
        setPassword(connectionId, userName, null);
    }

    public void setPassword(@NotNull ConnectionId connectionId, @NotNull String userName, @Nullable String password) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(connectionId, userName);
        Credentials credentials = StringUtil.isEmpty(password) ? null : new Credentials(userName, password);

        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        passwordSafe.set(credentialAttributes, credentials, false);
    }

    @Nullable
    public String getPassword(@NotNull ConnectionId connectionId, @NotNull String userName) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(connectionId, userName);

        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        Credentials credentials = passwordSafe.get(credentialAttributes);
        return credentials == null ? null : credentials.getPasswordAsString() ;
    }

    @NotNull
    private CredentialAttributes createCredentialAttributes(ConnectionId connectionId, String userName) {
        String serviceName = "DBNavigator.Connection." + connectionId;
        return new CredentialAttributes(serviceName, userName, this.getClass(), false);
    }

    private boolean isMemoryStorage() {
        return PasswordSafe.getInstance().isMemoryOnly();
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
