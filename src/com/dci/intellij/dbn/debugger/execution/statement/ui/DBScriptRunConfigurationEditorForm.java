package com.dci.intellij.dbn.debugger.execution.statement.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.debugger.execution.common.ui.DBProgramRunConfigurationEditorForm;
import com.dci.intellij.dbn.debugger.execution.statement.DBStatementRunConfiguration;

public class DBScriptRunConfigurationEditorForm extends DBProgramRunConfigurationEditorForm<DBStatementRunConfiguration>{
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JCheckBox compileDependenciesCheckBox;

    public DBScriptRunConfigurationEditorForm(final DBStatementRunConfiguration configuration) {
        super(configuration);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void writeConfiguration(DBStatementRunConfiguration configuration) {
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());
        //selectMethodAction.setConfiguration(configuration);
    }

    public void readConfiguration(DBStatementRunConfiguration configuration) {
        compileDependenciesCheckBox.setSelected(configuration.isCompileDependencies());
    }


    public void dispose() {
        super.dispose();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
