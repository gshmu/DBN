package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.common.config.DBRunConfigEditor;
import com.dci.intellij.dbn.debugger.jdbc.config.ui.DBStatementJdbcRunConfigurationEditorForm;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;

public class DBStatementJdbcRunConfigEditor extends DBRunConfigEditor<DBStatementJdbcRunConfig, DBStatementJdbcRunConfigurationEditorForm, StatementExecutionInput> {
    public DBStatementJdbcRunConfigEditor(DBStatementJdbcRunConfig configuration) {
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
