package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ui.ConnectionPropertiesSettingsForm;
import com.intellij.openapi.diagnostic.Logger;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBoolean;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBoolean;

@Getter
@Setter
public class ConnectionPropertiesSettings extends BasicProjectConfiguration<ConnectionSettings, ConnectionPropertiesSettingsForm> {
    public static final Logger LOGGER = LoggerFactory.createLogger();

    private Map<String, String> properties = new HashMap<>();
    private boolean enableAutoCommit = false;

    ConnectionPropertiesSettings(ConnectionSettings parent) {
        super(parent);
    }

    @NotNull
    @Override
    public ConnectionPropertiesSettingsForm createConfigurationEditor() {
        return new ConnectionPropertiesSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "properties";
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }

    /*********************************************************
    *                 PersistentConfiguration               *
    *********************************************************/
    @Override
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

    @Override
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
}
