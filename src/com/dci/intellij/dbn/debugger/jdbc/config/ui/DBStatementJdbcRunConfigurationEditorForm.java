package com.dci.intellij.dbn.debugger.jdbc.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.debugger.jdbc.config.DBStatementRunConfig;

public class DBStatementJdbcRunConfigurationEditorForm extends DBProgramRunConfigurationEditorForm<DBStatementRunConfig>{
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JCheckBox compileDependenciesCheckBox;

    public DBStatementJdbcRunConfigurationEditorForm(final DBStatementRunConfig configuration) {
        super(configuration);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void writeConfiguration(DBStatementRunConfig configuration) {
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());
        //selectMethodAction.setConfiguration(configuration);
    }

    public void readConfiguration(DBStatementRunConfig configuration) {
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
