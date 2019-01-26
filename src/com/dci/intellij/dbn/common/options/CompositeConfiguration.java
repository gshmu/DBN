package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.options.ConfigurationException;
import org.jdom.Element;

public abstract class CompositeConfiguration<T extends CompositeConfigurationEditorForm> extends Configuration<T> {
    private Configuration[] configurations;

    public final Configuration[] getConfigurations() {
        if (configurations == null) configurations = createConfigurations();
        return configurations;
    }

    protected abstract Configuration[] createConfigurations();

    @Override
    public final boolean isModified() {
        for (Configuration configuration : getConfigurations()) {
            if (configuration.isModified()) return true;
        }
        return super.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        T settingsEditor = getSettingsEditor();
        if (this instanceof TopLevelConfig && settingsEditor != null) {
            GUIUtil.stopTableCellEditing(settingsEditor.getComponent());
        }
        for (Configuration configuration : getConfigurations()) {
            configuration.apply();
        }
        super.apply();
        onApply();
    }

    @Override
    public final void reset() {
        for (Configuration configuration : getConfigurations()) {
            configuration.reset();
        }
        super.reset();
    }

    @Override
    public void disposeUIResources() {
        for (Configuration configuration : getConfigurations()) {
            configuration.disposeUIResources();
        }
        super.disposeUIResources();
    }

    @Override
    public void readConfiguration(Element element) {
        Configuration[] configurations = getConfigurations();
        for (Configuration configuration : configurations) {
            readConfiguration(element, configuration);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        Configuration[] configurations = getConfigurations();
        for (Configuration configuration : configurations) {
            writeConfiguration(element, configuration);
        }
    }
}
