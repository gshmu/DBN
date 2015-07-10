package com.dci.intellij.dbn.debugger.jdbc.config;

import com.dci.intellij.dbn.debugger.config.DBProgramRunConfigurationEditor;
import com.dci.intellij.dbn.debugger.jdbc.config.ui.DBStatementRunConfigurationEditorForm;

public class DBStatementRunConfigEditor extends DBProgramRunConfigurationEditor<DBStatementRunConfig, DBStatementRunConfigurationEditorForm> {
    public DBStatementRunConfigEditor(DBStatementRunConfig configuration) {
        super(configuration);
    }

    @Override
    protected DBStatementRunConfigurationEditorForm createConfigurationEditorForm() {
        return new DBStatementRunConfigurationEditorForm(getConfiguration());
    }
}
