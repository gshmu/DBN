package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionIdProvider;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.booleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.connectionIdAttribute;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionSettings extends CompositeProjectConfiguration<ConnectionBundleSettings, ConnectionSettingsForm>
        implements ConnectionRef, ConnectionIdProvider, Cloneable {

    private ConnectionId connectionId;
    private boolean active = true;
    private boolean isNew;

    private final ConnectionDatabaseSettings databaseSettings;
    private final ConnectionPropertiesSettings propertiesSettings = new ConnectionPropertiesSettings(this);
    private final ConnectionSshTunnelSettings sshTunnelSettings   = new ConnectionSshTunnelSettings(this);
    private final ConnectionSslSettings sslSettings               = new ConnectionSslSettings(this);
    private final ConnectionDetailSettings detailSettings         = new ConnectionDetailSettings(this);
    private final ConnectionFilterSettings filterSettings         = new ConnectionFilterSettings(this);

    public ConnectionSettings(ConnectionBundleSettings parent) {
        this(parent, DatabaseType.GENERIC, ConnectionConfigType.CUSTOM);
    }
    public ConnectionSettings(ConnectionBundleSettings parent, DatabaseType databaseType, ConnectionConfigType configType) {
        super(parent);
        databaseSettings = new ConnectionDatabaseSettings(this, databaseType, configType);
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                databaseSettings,
                propertiesSettings,
                sshTunnelSettings,
                sslSettings,
                detailSettings,
                filterSettings};
    }

    public void generateNewId() {
        connectionId = ConnectionId.create();
    }

    @NotNull
    @Override
    public ConnectionSettingsForm createConfigurationEditor() {
        return new ConnectionSettingsForm(this);
    }

    @Override
    public void readConfiguration(Element element) {
        if (ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.get()) {
            generateNewId();
        } else {
            connectionId = connectionIdAttribute(element, "id");
        }
        active = booleanAttribute(element, "active", active);
        super.readConfiguration(element);
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", connectionId.id());
        element.setAttribute("active", Boolean.toString(active));
        super.writeConfiguration(element);
    }

    @Override
    public ConnectionSettings clone() {
        Element connectionElement = new Element("Connection");
        writeConfiguration(connectionElement);
        ConnectionSettings clone = new ConnectionSettings(getParent() /*TODO config*/, databaseSettings.getDatabaseType(), databaseSettings.getConfigType());
        clone.readConfiguration(connectionElement);
        clone.databaseSettings.setConnectivityStatus(databaseSettings.getConnectivityStatus());
        clone.generateNewId();
        return clone;
    }

    @Override
    public String toString() {
        return connectionId.id();
    }
}
