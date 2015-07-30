package com.dci.intellij.dbn.debugger.jdwp.config;

import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigEditor;
import com.dci.intellij.dbn.debugger.jdwp.config.ui.DBStatementJdwpRunConfigEditorForm;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;

public class DBStatementJdwpRunConfigEditor extends DBProgramRunConfigEditor<DBStatementJdwpRunConfig, DBStatementJdwpRunConfigEditorForm, StatementExecutionInput> {
    public DBStatementJdwpRunConfigEditor(DBStatementJdwpRunConfig configuration) {
        super(configuration);
    }

    @Override
    protected DBStatementJdwpRunConfigEditorForm createConfigurationEditorForm() {
        return new DBStatementJdwpRunConfigEditorForm(getConfiguration());
    }


    public void setExecutionInput(StatementExecutionInput executionInput) {
    }
}
