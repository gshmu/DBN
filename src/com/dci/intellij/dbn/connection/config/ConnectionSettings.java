package com.dci.intellij.dbn.connection.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSettingsForm;

public class ConnectionSettings extends CompositeProjectConfiguration<ConnectionSettingsForm> implements ConnectionRef{
    private ConnectionBundleSettings parent;

    private String connectionId;
    private boolean isActive = true;
    private boolean isNew;

    private ConnectionDatabaseSettings databaseSettings;
    private ConnectionPropertiesSettings propertiesSettings;
    private ConnectionSshTunnelSettings sshTunnelSettings;
    private ConnectionDetailSettings detailSettings;
    private ConnectionFilterSettings filterSettings;
    private List<String> consoleNames = new ArrayList<String>();

    public ConnectionSettings(ConnectionBundleSettings parent) {
        this(parent, DatabaseType.UNKNOWN, ConnectionConfigType.BASIC);
    }
    public ConnectionSettings(ConnectionBundleSettings parent, DatabaseType databaseType, ConnectionConfigType configType) {
        super(parent.getProject());
        this.parent = parent;
        databaseSettings = new ConnectionDatabaseSettings(this, databaseType, configType);
        propertiesSettings = new ConnectionPropertiesSettings(this);
        sshTunnelSettings = new ConnectionSshTunnelSettings(this);
        detailSettings = new ConnectionDetailSettings(this);
        filterSettings = new ConnectionFilterSettings(this);
    }

    public ConnectionBundleSettings getParent() {
        return parent;
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
                detailSettings,
                filterSettings};
    }

    public void generateNewId() {
        connectionId =  UUID.randomUUID().toString();
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @NotNull
    @Override
    protected ConnectionSettingsForm createConfigurationEditor() {
        return new ConnectionSettingsForm(this);
    }

    public String getConnectionId() {
        return connectionId;
    }

    public List<String> getConsoleNames() {
        return consoleNames;
    }

    @Override
    public void readConfiguration(Element element) {
        if (ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.get()) {
            generateNewId();
        } else {
            connectionId = element.getAttributeValue("id");
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
        element.setAttribute("id", connectionId);
        element.setAttribute("active", Boolean.toString(isActive));
        super.writeConfiguration(element);
    }

    public ConnectionSettings clone() {
        try {
            Element connectionElement = new Element("Connection");
            writeConfiguration(connectionElement);
            ConnectionSettings clone = new ConnectionSettings(parent, databaseSettings.getDatabaseType(), databaseSettings.getConfigType());
            clone.readConfiguration(connectionElement);
            clone.databaseSettings.setConnectivityStatus(databaseSettings.getConnectivityStatus());
            clone.generateNewId();
            return clone;
        } catch (Exception e) {
            return null;
        }

    }
}
