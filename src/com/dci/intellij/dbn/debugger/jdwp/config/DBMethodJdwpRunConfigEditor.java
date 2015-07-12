package com.dci.intellij.dbn.debugger.jdwp.config;

import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigurationEditor;
import com.dci.intellij.dbn.debugger.jdwp.config.ui.DBMethodJdwpRunConfigEditorForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;

public class DBMethodJdwpRunConfigEditor extends DBProgramRunConfigurationEditor<DBMethodJdwpRunConfig, DBMethodJdwpRunConfigEditorForm> {
    public DBMethodJdwpRunConfigEditor(DBMethodJdwpRunConfig configuration) {
        super(configuration);
    }

    @Override
    protected DBMethodJdwpRunConfigEditorForm createConfigurationEditorForm() {
        return new DBMethodJdwpRunConfigEditorForm(getConfiguration());
    }


    public void setExecutionInput(MethodExecutionInput executionInput) {
        DBMethodJdwpRunConfigEditorForm configurationEditorForm = getConfigurationEditorForm(false);
        if (configurationEditorForm != null) {
            configurationEditorForm.setExecutionInput(executionInput, true);
        }
    }
}
