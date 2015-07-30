package com.dci.intellij.dbn.debugger.jdbc.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.debugger.common.config.ui.DBProgramRunConfigurationEditorForm;
import com.dci.intellij.dbn.debugger.jdbc.config.DBStatementJdbcRunConfig;

public class DBStatementJdbcRunConfigurationEditorForm extends DBProgramRunConfigurationEditorForm<DBStatementJdbcRunConfig> {
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JCheckBox compileDependenciesCheckBox;

    public DBStatementJdbcRunConfigurationEditorForm(final DBStatementJdbcRunConfig configuration) {
        super(configuration);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void writeConfiguration(DBStatementJdbcRunConfig configuration) {
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());
        //selectMethodAction.setConfiguration(configuration);
    }

    public void readConfiguration(DBStatementJdbcRunConfig configuration) {
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
