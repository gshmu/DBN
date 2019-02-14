package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 */
public class KeyValueSettings extends BasicConfiguration<Configuration, ConfigurationEditorForm> {
    private Map<String,String> settings = new HashMap<String, String>();

    private static final String YES = "YES";
    private static final String NO = "NO";

    public KeyValueSettings(Configuration parent) {
        super(parent);
    }

    public String getSetting(String key) {
        return settings.get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String setting = settings.get(key);
        if (setting != null) {
            return setting.equals(YES);
        } else {
            settings.put(key, defaultValue ? YES : NO);
            return defaultValue;
        }
    }

    public void setBoolean(String key, boolean value) {
        settings.put(key, value ? YES : NO);
    }

    public void registerSetting(String key, String value) {
        settings.put(key, value);
    }



    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        Element optionsElement = element.getChild("general");
        if (optionsElement != null) {
            for (Object o : optionsElement.getChildren()) {
                Element option = (Element) o;
                settings.put(option.getAttributeValue("key"), option.getAttributeValue("value"));
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        Element optionsElement = new Element("general");
        element.addContent(optionsElement);
        for (Object o : settings.keySet()) {
            String key = (String) o;
            String value = settings.get(key);
            Element option = new Element("setting");

            option.setAttribute("key", key );
            option.setAttribute("value", value );
            optionsElement.addContent(option);
        }
    }

    @Override
    @NotNull
    public ConfigurationEditorForm createConfigurationEditor() {
        throw new UnsupportedOperationException();
    }
}
