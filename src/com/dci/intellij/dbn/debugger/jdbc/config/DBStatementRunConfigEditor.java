package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigEditor;
import com.dci.intellij.dbn.debugger.jdbc.config.ui.DBStatementRunConfigurationEditorForm;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;

public class DBStatementRunConfigEditor extends DBProgramRunConfigEditor<DBStatementRunConfig, DBStatementRunConfigurationEditorForm, StatementExecutionInput> {
    public DBStatementRunConfigEditor(DBStatementRunConfig configuration) {
        super(configuration);
    }

    @Override
    protected DBStatementRunConfigurationEditorForm createConfigurationEditorForm() {
        return new DBStatementRunConfigurationEditorForm(getConfiguration());
    }

    @Override
    public void setExecutionInput(StatementExecutionInput executionInput) {

    }
}
