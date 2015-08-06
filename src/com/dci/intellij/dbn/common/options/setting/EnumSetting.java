package com.dci.intellij.dbn.common.options.setting;

import javax.swing.text.JTextComponent;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.intellij.openapi.options.ConfigurationException;

public class EnumSetting extends Setting<String, JTextComponent> implements PersistentConfiguration {
    public EnumSetting(String name, String value) {
        super(name, value);
    }
    
    @Override
    public void readConfiguration(Element parent) {
        setValue(SettingsUtil.getString(parent, getName(), this.value()));
    }

    @Override
    public void writeConfiguration(Element parent) {
        SettingsUtil.setString(parent, getName(), this.value());
    }

    @Override
    public boolean to(JTextComponent component) throws ConfigurationException {
        return setValue(component.getText());
    }

    @Override
    public void from(JTextComponent component) {
        component.setText(value());
    }

}
