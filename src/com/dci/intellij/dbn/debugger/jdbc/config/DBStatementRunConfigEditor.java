package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigEditor;
import com.dci.intellij.dbn.debugger.jdbc.config.ui.DBStatementJdbcRunConfigurationEditorForm;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;

public class DBStatementRunConfigEditor extends DBProgramRunConfigEditor<DBStatementRunConfig, DBStatementJdbcRunConfigurationEditorForm, StatementExecutionInput> {
    public DBStatementRunConfigEditor(DBStatementRunConfig configuration) {
        super(configuration);
    }

    @Override
    protected DBStatementJdbcRunConfigurationEditorForm createConfigurationEditorForm() {
        return new DBStatementJdbcRunConfigurationEditorForm(getConfiguration());
    }

    @Override
    public void setExecutionInput(StatementExecutionInput executionInput) {

    }
}
