package com.dci.intellij.dbn.connection.config;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSettingsForm;

public class ConnectionSettings extends CompositeProjectConfiguration<ConnectionSettingsForm> {
    private ConnectionBundleSettings parent;

    private ConnectionDatabaseSettings databaseSettings;
    private ConnectionDetailSettings detailSettings;
    private ConnectionFilterSettings filterSettings;

    public ConnectionSettings(ConnectionBundleSettings parent) {
        super(parent.getProject());
        this.parent = parent;
        databaseSettings = new GenericConnectionDatabaseSettings(this);
        detailSettings = new ConnectionDetailSettings(this);
        filterSettings = new ConnectionFilterSettings(this);
    }

    public ConnectionBundleSettings getParent() {
        return parent;
    }

    public ConnectionDatabaseSettings getDatabaseSettings() {
        return databaseSettings;
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
                detailSettings,
                filterSettings};
    }

    @Override
    protected ConnectionSettingsForm createConfigurationEditor() {
        return new ConnectionSettingsForm(this);
    }

    public String getConnectionId() {
        return databaseSettings.getId();
    }

    @NotNull
    @Override
    public String getId() {
        return databaseSettings.getId();
    }


    public ConnectionSettings clone() {
        try {
            Element connectionElement = new Element("Connection");
            writeConfiguration(connectionElement);
            ConnectionSettings clone = new ConnectionSettings(parent);
            clone.readConfiguration(connectionElement);
            clone.getDatabaseSettings().setConnectivityStatus(databaseSettings.getConnectivityStatus());
            clone.getDatabaseSettings().generateNewId();
            return clone;
        } catch (Exception e) {
        }
        return null;
    }
}
