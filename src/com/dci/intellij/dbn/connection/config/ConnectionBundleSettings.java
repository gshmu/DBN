package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.util.ThreadLocalFlag;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConnectionBundleSettings extends ProjectConfiguration<ConnectionBundleSettingsForm> implements TopLevelConfig {
    public static ThreadLocalFlag IS_IMPORT_EXPORT_ACTION = new ThreadLocalFlag(false);
    public List<ConnectionSettings> connections = new ArrayList<ConnectionSettings>();

    public ConnectionBundleSettings(Project project) {
        super(project);
    }

    public static ConnectionBundleSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getConnectionSettings();
    }

    public ConnectionSettings getConnectionSettings(ConnectionId connectionId) {
        for (ConnectionSettings connection : connections) {
            if (connection.getConnectionId() == connectionId) {
                return connection;
            }
        }
        return null;
    }

    public List<ConnectionSettings> getConnections() {
        return connections;
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.ConnectionSettings";
    }

    public String getDisplayName() {
        return "Connections";
    }

    public String getHelpTopic() {
        return "connectionBundle";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.CONNECTIONS;
    }

    @Override
    public String getConfigElementName() {
        return "connections";
    }

    public boolean isModified() {
        if (super.isModified()) {
            return true;
        }
        for (ConnectionSettings connectionSetting : connections) {
            if (connectionSetting.isModified() || connectionSetting.isNew()) return true;
        }
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        for (ConnectionSettings connectionSetting : connections) {
            connectionSetting.reset();
        }
    }

    @NotNull
    @Override
    public Configuration<ConnectionBundleSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
    *                   UnnamedConfigurable                 *
    *********************************************************/
    @NotNull
    public ConnectionBundleSettingsForm createConfigurationEditor() {
        return new ConnectionBundleSettingsForm(this);
    }

    /*********************************************************
     *                      Configurable                     *
     *********************************************************/
    public void readConfiguration(Element element) {
        Project project = getProject();
        connections.clear();
        for (Object o : element.getChildren()) {
            Element connectionElement = (Element) o;
            ConnectionSettings connection = new ConnectionSettings(this);
            connection.readConfiguration(connectionElement);
            connections.add(connection);
        }

        if (!project.isDefault() && !isTransitory()) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
            connectionBundle.applySettings(this);
        }
    }

    public void writeConfiguration(Element element) {
        for (ConnectionSettings connectionSetting : connections) {
            Element connectionElement = new Element("connection");
            connectionSetting.writeConfiguration(connectionElement);
            element.addContent(connectionElement);
        }
    }

    @Override
    public void disposeUIResources() {
        super.disposeUIResources();
        for (ConnectionSettings connectionSetting : connections) {
            connectionSetting.disposeUIResources();
        }
    }
}
