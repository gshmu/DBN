package com.dci.intellij.dbn.common.options.setting;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import org.jdom.Element;

import javax.swing.*;

import static com.dci.intellij.dbn.common.options.setting.Settings.*;

public class BooleanSetting extends Setting<Boolean, JToggleButton> implements PersistentConfiguration {
    public BooleanSetting(String name, Boolean value) {
        super(name, value);
    }
    
    @Override
    public void readConfiguration(Element parent) {
        setValue(getBoolean(parent, getName(), this.value()));
    }

    public void readConfigurationAttribute(Element parent) {
        setValue(booleanAttribute(parent, getName(), this.value()));
    }

    @Override
    public void writeConfiguration(Element parent) {
        setBoolean(parent, getName(), this.value());
    }

    public void writeConfigurationAttribute(Element parent) {
        setBooleanAttribute(parent, getName(), this.value());
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
