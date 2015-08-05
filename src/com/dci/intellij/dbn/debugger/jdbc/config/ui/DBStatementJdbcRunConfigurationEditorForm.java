package com.dci.intellij.dbn.debugger.jdbc.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.config.ui.DBProgramRunConfigurationEditorForm;
import com.dci.intellij.dbn.debugger.jdbc.config.DBStatementJdbcRunConfig;

public class DBStatementJdbcRunConfigurationEditorForm extends DBProgramRunConfigurationEditorForm<DBStatementJdbcRunConfig> {
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JCheckBox compileDependenciesCheckBox;
    private JPanel hintPanel;

    public DBStatementJdbcRunConfigurationEditorForm(final DBStatementJdbcRunConfig configuration) {
        super(configuration.getProject());
        if (configuration.getCategory() != DBRunConfigCategory.CUSTOM) {
            headerPanel.setVisible(false);
            DBNHintForm hintForm = new DBNHintForm(DatabaseDebuggerManager.GENERIC_STATEMENT_RUNNER_HINT, null, true);
            hintPanel.setVisible(true);
            hintPanel.add(hintForm.getComponent());
        } else {
            hintPanel.setVisible(false);
        }

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
