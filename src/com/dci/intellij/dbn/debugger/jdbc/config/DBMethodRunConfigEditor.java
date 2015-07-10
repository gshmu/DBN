package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.jdbc.config.ui.DBMethodRunConfigurationEditorForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;

public class DBMethodRunConfigEditor extends DBProgramRunConfigurationEditor<DBMethodRunConfig, DBMethodRunConfigurationEditorForm> {
    public DBMethodRunConfigEditor(DBMethodRunConfig configuration) {
        super(configuration);
    }

    @Override
    protected DBMethodRunConfigurationEditorForm createConfigurationEditorForm() {
        return new DBMethodRunConfigurationEditorForm(getConfiguration());
    }


    public void setExecutionInput(MethodExecutionInput executionInput) {
        DBMethodRunConfigurationEditorForm configurationEditorForm = getConfigurationEditorForm(false);
        if (configurationEditorForm != null) {
            configurationEditorForm.setExecutionInput(executionInput, true);
        }
    }
}
