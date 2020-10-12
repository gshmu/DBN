package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.options.ConfigurationException;
import org.jdom.Element;

public abstract class CompositeConfiguration<P extends Configuration, E extends CompositeConfigurationEditorForm>
        extends BasicConfiguration<P, E> {

    private Configuration[] configurations;

    public CompositeConfiguration(P parent) {
        super(parent);
    }

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
        E settingsEditor = getSettingsEditor();
        if (this instanceof TopLevelConfig && settingsEditor != null) {
            GUIUtil.stopTableCellEditing(settingsEditor.getComponent());
        }
        for (Configuration configuration : getConfigurations()) {
            configuration.apply();
        }
        super.apply();
    }

    @Override
    public final void reset() {
        for (Configuration configuration : getConfigurations()) {
            configuration.reset();
        }
        super.reset();
    }

    @Override
    public final void disposeUIResources() {
        super.disposeUIResources();
        for (Configuration configuration : getConfigurations()) {
            configuration.disposeUIResources();
        }
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
