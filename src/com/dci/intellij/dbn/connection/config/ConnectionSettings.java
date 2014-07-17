package com.dci.intellij.dbn.connection.config;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.config.ui.ConnectionSettingsForm;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

public class ConnectionSettings extends CompositeProjectConfiguration<ConnectionSettingsForm> {
    private ConnectionDatabaseSettings databaseSettings;
    private ConnectionDetailSettings detailSettings;
    private ConnectionFilterSettings filterSettings;

    public ConnectionSettings(ConnectionBundle connectionBundle) {
        super(connectionBundle.getProject());
        databaseSettings = new GenericConnectionDatabaseSettings(connectionBundle, this);
        detailSettings = new ConnectionDetailSettings(this);
        filterSettings = new ConnectionFilterSettings(this);
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
            ConnectionSettings clone = new ConnectionSettings(databaseSettings.getConnectionBundle());
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
    public void readConfiguration(Element element) throws InvalidDataException {
        if (element.getChild(databaseSettings.getConfigElementName()) != null) {
            readConfiguration(element, databaseSettings);
        } else {
            // TODO: decommission (support old configuration)
            databaseSettings.readConfiguration(element);
        }
        readConfiguration(element, detailSettings);
        readConfiguration(element, filterSettings);
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        writeConfiguration(element, databaseSettings);
        writeConfiguration(element, detailSettings);
        writeConfiguration(element, filterSettings);
    }

    public String getConnectionId() {
        return databaseSettings.getId();
    }
}
