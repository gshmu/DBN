package com.dci.intellij.dbn.debugger.execution.method;

import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationEditor;
import com.dci.intellij.dbn.debugger.execution.method.ui.DBMethodRunConfigurationEditorForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;

public class DBMethodRunConfigurationEditor extends DBProgramRunConfigurationEditor<DBMethodRunConfiguration, DBMethodRunConfigurationEditorForm> {
    public DBMethodRunConfigurationEditor(DBMethodRunConfiguration configuration) {
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
