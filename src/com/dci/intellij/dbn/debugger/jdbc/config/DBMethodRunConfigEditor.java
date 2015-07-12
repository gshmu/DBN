package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigurationEditor;
import com.dci.intellij.dbn.debugger.jdbc.config.ui.DBMethodRunConfigEditorForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;

public class DBMethodRunConfigEditor extends DBProgramRunConfigurationEditor<DBMethodRunConfig, DBMethodRunConfigEditorForm> {
    public DBMethodRunConfigEditor(DBMethodRunConfig configuration) {
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
