package com.dci.intellij.dbn.credentials;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.BaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class DatabaseCredentialManager implements BaseComponent {

    public static DatabaseCredentialManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseCredentialManager.class);
    }


    public void removePassword(ConnectionId connectionId, String userName) {
        setPassword(connectionId, userName, null);
    }

    public void setPassword(ConnectionId connectionId, String userName, @Nullable String password) {
        SimpleLaterInvocator.invoke(ModalityState.NON_MODAL, () -> {
            String serviceName = getConnectionServiceName(connectionId);
            CredentialAttributes credentialAttributes = new CredentialAttributes(serviceName, userName, null, false);
            PasswordSafe.getInstance().setPassword(credentialAttributes, password);
        });
    }

    @Nullable
    public String getPassword(ConnectionId connectionId, String userName) {
        AtomicReference<String> password = new AtomicReference<>();
        SimpleLaterInvocator.invoke(ModalityState.NON_MODAL, () -> {
            String serviceName = getConnectionServiceName(connectionId);
            PasswordSafe passwordSafe = PasswordSafe.getInstance();
            CredentialAttributes credentialAttributes = new CredentialAttributes(serviceName, userName, null, false);
            String pwd = passwordSafe.getPassword(credentialAttributes);
            password.set(pwd);
        });
        return password.get();
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
