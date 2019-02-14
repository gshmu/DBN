package com.dci.intellij.dbn.common.options.setting;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import org.jdom.Element;

import javax.swing.*;

public class BooleanSetting extends Setting<Boolean, JToggleButton> implements PersistentConfiguration {
    public BooleanSetting(String name, Boolean value) {
        super(name, value);
    }
    
    @Override
    public void readConfiguration(Element parent) {
        setValue(SettingsSupport.getBoolean(parent, getName(), this.value()));
    }

    public void readConfigurationAttribute(Element parent) {
        setValue(SettingsSupport.getBooleanAttribute(parent, getName(), this.value()));
    }

    @Override
    public void writeConfiguration(Element parent) {
        SettingsSupport.setBoolean(parent, getName(), this.value());
    }

    public void writeConfigurationAttribute(Element parent) {
        SettingsSupport.setBooleanAttribute(parent, getName(), this.value());
    }


    @Override
    public boolean to(JToggleButton checkBox) {
        return setValue(checkBox.isSelected());
    }
    
    @Override
    public void from(JToggleButton checkBox) {
        checkBox.setSelected(value());
    }
}
