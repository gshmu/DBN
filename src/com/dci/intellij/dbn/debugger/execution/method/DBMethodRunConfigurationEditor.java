package com.dci.intellij.dbn.debugger.execution.method;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.execution.method.ui.DBMethodRunConfigurationEditorForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;

public class DBMethodRunConfigurationEditor extends SettingsEditor<DBMethodRunConfiguration> {
    private DBMethodRunConfigurationEditorForm configurationEditorComponent;
    private DBMethodRunConfiguration configuration;

    public DBMethodRunConfigurationEditor(DBMethodRunConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void resetEditorFrom(DBMethodRunConfiguration configuration) {
        configurationEditorComponent.readConfiguration(configuration);
    }

    @Override
    protected void applyEditorTo(DBMethodRunConfiguration configuration) throws ConfigurationException {
        configurationEditorComponent.writeConfiguration(configuration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        configurationEditorComponent = new DBMethodRunConfigurationEditorForm(configuration);
        return configurationEditorComponent.getComponent();
    }

    @Override
    protected void disposeEditor() {
        //configurationEditorComponent.dispose();
    }

    public void setExecutionInput(MethodExecutionInput executionInput) {
        if (configurationEditorComponent != null) {
            configurationEditorComponent.setExecutionInput(executionInput, true);
        }
    }
}
