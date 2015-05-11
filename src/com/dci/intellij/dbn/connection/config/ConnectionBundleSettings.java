package com.dci.intellij.dbn.connection.config;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.util.ThreadLocalFlag;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;

public class ConnectionBundleSettings extends ProjectConfiguration<ConnectionBundleSettingsForm> implements TopLevelConfig {
    public static ThreadLocalFlag IS_IMPORT_EXPORT_ACTION = new ThreadLocalFlag(false);
    public List<ConnectionSettings> connections = new ArrayList<ConnectionSettings>();

    public ConnectionBundleSettings(Project project) {
        super(project);
    }

    public static ConnectionBundleSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getConnectionSettings();
    }

    public ConnectionSettings getConnectionSettings(String connectionId) {
        for (ConnectionSettings connection : connections) {
            if (connection.getConnectionId().equals(connectionId)) {
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

    @Override
    protected Configuration<ConnectionBundleSettingsForm> getOriginalSettings() {
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

            Element consolesElement = connectionElement.getChild("consoles");
            if (consolesElement != null) {
                for (Object c : consolesElement.getChildren()) {
                    Element consoleElement = (Element) c;
                    String consoleName = consoleElement.getAttributeValue("name");
                    connection.getConsoleNames().add(consoleName);
                }
            }
        }

        if (!project.isDefault()) {
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

            Element consolesElement = new Element("consoles");
            connectionElement.addContent(consolesElement);
            for (String consoleName : connectionSetting.getConsoleNames()) {
                Element consoleElement = new Element("console");
                consoleElement.setAttribute("name", consoleName);
                consolesElement.addContent(consoleElement);
            }
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
