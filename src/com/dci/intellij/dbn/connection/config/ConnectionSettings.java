package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

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
        return new Configuration[] {databaseSettings, detailSettings, filterSettings};
    }

    @Override
    protected ConnectionSettingsForm createConfigurationEditor() {
        return new ConnectionSettingsForm(this);
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


    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public void readConfiguration(Element element) {
        if (element.getChild(databaseSettings.getConfigElementName()) != null) {
            readConfiguration(element, databaseSettings);
        } else {
            // TODO: decommission (support old configuration)
            databaseSettings.readConfiguration(element);
        }
        readConfiguration(element, detailSettings);
        readConfiguration(element, filterSettings);
    }

    public void writeConfiguration(Element element) {
        writeConfiguration(element, databaseSettings);
        writeConfiguration(element, detailSettings);
        writeConfiguration(element, filterSettings);
    }

    public String getConnectionId() {
        return databaseSettings.getId();
    }
}
