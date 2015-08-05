package com.dci.intellij.dbn.common.options.setting;

import javax.swing.JToggleButton;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;

public class BooleanSetting extends Setting<Boolean, JToggleButton> implements PersistentConfiguration {
    public BooleanSetting(String name, Boolean value) {
        super(name, value);
    }
    
    @Override
    public void readConfiguration(Element parent) {
        setValue(SettingsUtil.getBoolean(parent, getName(), this.value()));
    }

    public void readConfigurationAttribute(Element parent) {
        setValue(SettingsUtil.getBooleanAttribute(parent, getName(), this.value()));
    }

    @Override
    public void writeConfiguration(Element parent) {
        SettingsUtil.setBoolean(parent, getName(), this.value());
    }

    public void writeConfigurationAttribute(Element parent) {
        SettingsUtil.setBooleanAttribute(parent, getName(), this.value());
    }


    public boolean to(JToggleButton checkBox) {
        return setValue(checkBox.isSelected());
    }
    
    public void from(JToggleButton checkBox) {
        checkBox.setSelected(value());
    }
}
