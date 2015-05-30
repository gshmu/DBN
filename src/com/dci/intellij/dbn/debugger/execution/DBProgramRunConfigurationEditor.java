package com.dci.intellij.dbn.debugger.execution;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.debugger.execution.common.ui.DBProgramRunConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public abstract class DBProgramRunConfigurationEditor<T extends DBProgramRunConfiguration, F extends DBProgramRunConfigurationEditorForm<T>> extends SettingsEditor<T> {
    private T configuration;
    private F configurationEditorForm;

    public DBProgramRunConfigurationEditor(T configuration) {
        this.configuration = configuration;
    }

    public T getConfiguration() {
        return configuration;
    }

    protected abstract F createConfigurationEditorForm();

    public F getConfigurationEditorForm(boolean create) {
        if (create && (configurationEditorForm == null || configurationEditorForm.isDisposed())) {
            configurationEditorForm = createConfigurationEditorForm();
        }
        return configurationEditorForm;
    }

    @Override
    protected void disposeEditor() {
        DisposerUtil.dispose(configurationEditorForm);
        configurationEditorForm = null;
    }

    @Override
    protected void resetEditorFrom(T configuration) {
        getConfigurationEditorForm(true).readConfiguration(configuration);
    }

    @Override
    protected void applyEditorTo(T configuration) throws ConfigurationException {
        getConfigurationEditorForm(true).writeConfiguration(configuration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        configurationEditorForm = getConfigurationEditorForm(true);
        return configurationEditorForm.getComponent();
    }
}
