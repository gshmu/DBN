package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.options.CompositeConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ui.ConnectionFilterSettingsForm;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterSettings;
import com.dci.intellij.dbn.object.filter.type.ObjectTypeFilterSettings;
import com.intellij.openapi.project.Project;

public class ConnectionFilterSettings extends CompositeConfiguration<ConnectionFilterSettingsForm> {
    private ObjectTypeFilterSettings objectTypeFilterSettings;
    private ObjectNameFilterSettings objectNameFilterSettings;
    private ConnectionSettings connectionSettings;

    public ConnectionFilterSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
        Project project = connectionSettings.getProject();
        DatabaseBrowserSettings databaseBrowserSettings = DatabaseBrowserSettings.getInstance(project);
        ObjectTypeFilterSettings master = databaseBrowserSettings.getFilterSettings().getObjectTypeFilterSettings();
        objectTypeFilterSettings = new ObjectTypeFilterSettings(project, master);
        objectNameFilterSettings = new ObjectNameFilterSettings(project);
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public String getDisplayName() {
        return "Connection Filter Settings";
    }

    public String getHelpTopic() {
        return "connectionFilterSettings";
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public ObjectTypeFilterSettings getObjectTypeFilterSettings() {
        return objectTypeFilterSettings;
    }

    public ObjectNameFilterSettings getObjectNameFilterSettings() {
        return objectNameFilterSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    public ConnectionFilterSettingsForm createConfigurationEditor() {
        return new ConnectionFilterSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "object-filters";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {objectTypeFilterSettings, objectNameFilterSettings};
    }
}
