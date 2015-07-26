package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigurationEditor;
import com.dci.intellij.dbn.debugger.jdbc.config.ui.DBMethodRunConfigEditorForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;

public class DBMethodJdbcRunConfigEditor extends DBProgramRunConfigurationEditor<DBMethodJdbcRunConfig, DBMethodRunConfigEditorForm, MethodExecutionInput> {
    public DBMethodJdbcRunConfigEditor(DBMethodJdbcRunConfig configuration) {
        super(configuration);
    }

    @Override
    protected DBMethodRunConfigEditorForm createConfigurationEditorForm() {
        return new DBMethodRunConfigEditorForm(getConfiguration());
    }


    public void setExecutionInput(MethodExecutionInput executionInput) {
        DBMethodRunConfigEditorForm configurationEditorForm = getConfigurationEditorForm(false);
        if (configurationEditorForm != null) {
            configurationEditorForm.setExecutionInput(executionInput, true);
        }
    }
}
