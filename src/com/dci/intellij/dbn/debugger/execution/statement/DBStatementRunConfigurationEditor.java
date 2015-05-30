package com.dci.intellij.dbn.debugger.execution.statement;

import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationEditor;
import com.dci.intellij.dbn.debugger.execution.statement.ui.DBScriptRunConfigurationEditorForm;

public class DBStatementRunConfigurationEditor extends DBProgramRunConfigurationEditor<DBStatementRunConfiguration, DBScriptRunConfigurationEditorForm> {
    public DBStatementRunConfigurationEditor(DBStatementRunConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected DBScriptRunConfigurationEditorForm createConfigurationEditorForm() {
        return new DBScriptRunConfigurationEditorForm(getConfiguration());
    }
}
