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

    @Override
    protected void disposeEditor() {
        //configurationEditorComponent.dispose();
    }

    public void setExecutionInput(MethodExecutionInput executionInput) {
        DBMethodRunConfigurationEditorForm configurationEditorForm = getConfigurationEditorForm();
        if (configurationEditorForm != null) {
            configurationEditorForm.setExecutionInput(executionInput, true);
        }
    }
}
