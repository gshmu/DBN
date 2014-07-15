package com.dci.intellij.dbn.common.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

public class ConfigurationUtil extends SettingsUtil{

    public static void writeConfiguration(Element element, Configuration configuration) throws WriteExternalException {
        String elementName = configuration.getConfigElementName();
        if (elementName != null) {
            Element childElement = new Element(elementName);
            element.addContent(childElement);
            configuration.writeConfiguration(childElement);
        }
    }


    public static void readConfiguration(Element element, Configuration configuration) throws InvalidDataException {
        String elementName = configuration.getConfigElementName();
        if (elementName != null) {
            Element childElement = element.getChild(elementName);
            if (childElement != null) {
                configuration.readConfiguration(childElement);
            }
        }
    }

}
