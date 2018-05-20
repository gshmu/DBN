package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ui.ConnectionPropertiesSettingsForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConnectionPropertiesSettings extends Configuration<ConnectionPropertiesSettingsForm> {
    public static final Logger LOGGER = LoggerFactory.createLogger();
    private ConnectionSettings parent;

    private Map<String, String> properties = new HashMap<String, String>();
    private boolean enableAutoCommit = false;

    public ConnectionPropertiesSettings(ConnectionSettings parent) {
        this.parent = parent;
    }

    public ConnectionSettings getParent() {
        return parent;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean isEnableAutoCommit() {
        return enableAutoCommit;
    }

    public void setEnableAutoCommit(boolean isAutoCommit) {
        this.enableAutoCommit = isAutoCommit;
    }

    @NotNull
    @Override
    protected ConnectionPropertiesSettingsForm createConfigurationEditor() {
        return new ConnectionPropertiesSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "properties";
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return parent.getConnectionId();
    }

    /*********************************************************
    *                 PersistentConfiguration               *
    *********************************************************/
    public void readConfiguration(Element element) {
        enableAutoCommit = getBoolean(element, "auto-commit", enableAutoCommit);
        Element propertiesElement = element.getChild("properties");
        if (propertiesElement != null) {
            for (Object o : propertiesElement.getChildren()) {
                Element propertyElement = (Element) o;
                properties.put(
                        propertyElement.getAttributeValue("key"),
                        propertyElement.getAttributeValue("value"));
            }
        }
        getParent().getDatabaseSettings().updateHashCode();
    }

    public void writeConfiguration(Element element) {
        setBoolean(element, "auto-commit", enableAutoCommit);
        if (properties.size() > 0) {
            Element propertiesElement = new Element("properties");
            for (String propertyKey : properties.keySet()) {
                Element propertyElement = new Element("property");
                propertyElement.setAttribute("key", propertyKey);
                propertyElement.setAttribute("value", CommonUtil.nvl(properties.get(propertyKey), ""));

                propertiesElement.addContent(propertyElement);
            }
            element.addContent(propertiesElement);
        }
    }

    public Project getProject() {
        return parent.getProject();
    }
}
