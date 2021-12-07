package com.dci.intellij.dbn.common.database;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.AuthenticationType;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.PasswordUtil;
import com.dci.intellij.dbn.credentials.DatabaseCredentialManager;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class AuthenticationInfo extends BasicConfiguration<ConnectionDatabaseSettings, ConfigurationEditorForm> implements Cloneable<AuthenticationInfo>{
    @Deprecated // TODO move to keychain
    private static final String OLD_PWD_ATTRIBUTE = "password";
    @Deprecated // TODO move to keychain
    private static final String TEMP_PWD_ATTRIBUTE = "deprecated-pwd";

    private final long timestamp = System.currentTimeMillis();

    private AuthenticationType type = AuthenticationType.USER_PASSWORD;
    private String user;
    private String password;
    private boolean temporary;

    public AuthenticationInfo(ConnectionDatabaseSettings parent, boolean temporary) {
        super(parent);
        this.temporary = temporary;
    }

    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }

    public void setPassword(String password) {
        this.password = Strings.isEmpty(password) ? null : password;
    }

    public boolean isProvided() {
        switch (type) {
            case NONE: return true;
            case USER: return Strings.isNotEmpty(user);
            case USER_PASSWORD: return Strings.isNotEmpty(user) && Strings.isNotEmpty(password);
            case OS_CREDENTIALS: return true;
        }
        return true;
    }

    public boolean isOlderThan(long millis) {
        return TimeUtil.isOlderThan(timestamp, millis);
    }

    public boolean isSame(AuthenticationInfo authenticationInfo) {
        return
            this.type == authenticationInfo.type &&
            Safe.equal(this.user, authenticationInfo.user) &&
            Safe.equal(this.getPassword(), authenticationInfo.getPassword());
    }

    @Override
    public void readConfiguration(Element element) {
        user = getString(element, "user", user);
        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();

        if (DatabaseCredentialManager.USE) {
            password = credentialManager.getPassword(getConnectionId(), user);
        }

        // old storage fallback - TODO cleanup
        if (Strings.isEmpty(password)) {
            password = PasswordUtil.decodePassword(getString(element, TEMP_PWD_ATTRIBUTE, password));
            if (Strings.isEmpty(password)) {
                password = PasswordUtil.decodePassword(getString(element, OLD_PWD_ATTRIBUTE, password));
            }

            if (Strings.isNotEmpty(this.password) && DatabaseCredentialManager.USE) {
                credentialManager.setPassword(getConnectionId(), user, this.password);
            }
        }

        type = getEnum(element, "type", type);

        AuthenticationType[] supportedAuthTypes = getParent().getDatabaseType().getAuthTypes();
        if (!type.isOneOf(supportedAuthTypes)) {
            type = supportedAuthTypes[0];
        }

        // TODO backward compatibility
        if (getBoolean(element, "os-authentication", false)) {
            type = AuthenticationType.OS_CREDENTIALS;
        } else if (getBoolean(element, "empty-authentication", false)) {
            type = AuthenticationType.USER;
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        setEnum(element, "type", type);
        setString(element, "user", nvl(user));

        String encodedPassword = PasswordUtil.encodePassword(password);
        if (!DatabaseCredentialManager.USE){
            setString(element, TEMP_PWD_ATTRIBUTE, encodedPassword);
        }
    }

    @Override
    public AuthenticationInfo clone() {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo(getParent(), temporary);
        authenticationInfo.type = type;
        authenticationInfo.user = user;
        authenticationInfo.password = password;
        return authenticationInfo;
    }

    public void updateKeyChain(String oldUserName, String oldPassword) {
        if (type == AuthenticationType.USER_PASSWORD && !temporary && DatabaseCredentialManager.USE) {
            oldUserName = nvl(oldUserName);
            oldPassword = nvl(oldPassword);

            String newUserName = nvl(user);
            String newPassword = nvl(password);

            boolean userNameChanged = !Safe.equal(oldUserName, newUserName);
            boolean passwordChanged = !Safe.equal(oldPassword, newPassword);
            if (userNameChanged || passwordChanged) {
                DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
                ConnectionId connectionId = getConnectionId();

                if (userNameChanged) {
                    credentialManager.removePassword(connectionId, oldUserName);
                }
                if (Strings.isNotEmpty(newUserName) && Strings.isNotEmpty(newPassword)) {
                    credentialManager.setPassword(connectionId, newUserName, newPassword);
                }
            }
        }
    }
}
