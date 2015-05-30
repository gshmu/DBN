package com.dci.intellij.dbn.debugger.execution;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.execution.common.ui.DBProgramRunConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;

public abstract class DBProgramRunConfigurationEditor<T extends DBProgramRunConfiguration, F extends DBProgramRunConfigurationEditorForm<T>> extends SettingsEditor<T> {
    private T configuration;
    private F configurationEditorForm;

    public DBProgramRunConfigurationEditor(T configuration) {
        this.configuration = configuration;
        this.configurationEditorForm = createConfigurationEditorForm();
    }

    public T getConfiguration() {
        return configuration;
    }

    protected abstract F createConfigurationEditorForm();

    public F getConfigurationEditorForm() {
        return configurationEditorForm;
    }

    @Override
    protected void resetEditorFrom(T configuration) {
        configurationEditorForm.readConfiguration(configuration);
    }

    @Override
    protected void applyEditorTo(T configuration) throws ConfigurationException {
        configurationEditorForm.writeConfiguration(configuration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        configurationEditorForm = createConfigurationEditorForm();
        return configurationEditorForm.getComponent();
    }
}
