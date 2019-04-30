package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionIdProvider;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBooleanAttribute;

public class ConnectionSettings extends CompositeProjectConfiguration<ConnectionBundleSettings, ConnectionSettingsForm>
        implements ConnectionRef, ConnectionIdProvider, Cloneable {

    private ConnectionId connectionId;
    private boolean isActive = true;
    private boolean isNew;

    private ConnectionDatabaseSettings databaseSettings;
    private ConnectionPropertiesSettings propertiesSettings = new ConnectionPropertiesSettings(this);
    private ConnectionSshTunnelSettings sshTunnelSettings   = new ConnectionSshTunnelSettings(this);
    private ConnectionSslSettings sslSettings               = new ConnectionSslSettings(this);
    private ConnectionDetailSettings detailSettings         = new ConnectionDetailSettings(this);
    private ConnectionFilterSettings filterSettings         = new ConnectionFilterSettings(this);

    public ConnectionSettings(ConnectionBundleSettings parent) {
        this(parent, DatabaseType.GENERIC, ConnectionConfigType.CUSTOM);
    }
    public ConnectionSettings(ConnectionBundleSettings parent, DatabaseType databaseType, ConnectionConfigType configType) {
        super(parent);
        databaseSettings = new ConnectionDatabaseSettings(this, databaseType, configType);
    }

    public ConnectionDatabaseSettings getDatabaseSettings() {
        return databaseSettings;
    }

    public ConnectionPropertiesSettings getPropertiesSettings() {
        return propertiesSettings;
    }

    public ConnectionSshTunnelSettings getSshTunnelSettings() {
        return sshTunnelSettings;
    }

    public ConnectionSslSettings getSslSettings() {
        return sslSettings;
    }

    public ConnectionDetailSettings getDetailSettings() {
        return detailSettings;
    }

    public ConnectionFilterSettings getFilterSettings() {
        return filterSettings;
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

    public void setConnectionId(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @NotNull
    @Override
    public ConnectionSettingsForm createConfigurationEditor() {
        return new ConnectionSettingsForm(this);
    }

    @Override
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Override
    public void readConfiguration(Element element) {
        if (ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.get()) {
            generateNewId();
        } else {
            connectionId = ConnectionId.get(element.getAttributeValue("id"));
        }
        isActive = getBooleanAttribute(element, "active", isActive);
        super.readConfiguration(element);
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", connectionId.id());
        element.setAttribute("active", Boolean.toString(isActive));
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
}
