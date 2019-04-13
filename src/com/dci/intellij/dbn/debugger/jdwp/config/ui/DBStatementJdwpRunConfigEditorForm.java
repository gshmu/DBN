package com.dci.intellij.dbn.debugger.jdwp.config.ui;

import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.config.ui.DBProgramRunConfigurationEditorForm;
import com.dci.intellij.dbn.debugger.jdwp.config.DBStatementJdwpRunConfig;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.Range;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DBStatementJdwpRunConfigEditorForm extends DBProgramRunConfigurationEditorForm<DBStatementJdwpRunConfig>{
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JCheckBox compileDependenciesCheckBox;
    private JTextField fromPortTextField;
    private JTextField toPortTextField;
    private JPanel hintPanel;

    private StatementExecutionInput executionInput;

    public DBStatementJdwpRunConfigEditorForm(final DBStatementJdwpRunConfig configuration) {
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

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    @Override
    public void writeConfiguration(DBStatementJdwpRunConfig configuration) throws ConfigurationException {
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());

        int fromPort = 0;
        int toPort = 0;
        try {
            fromPort = Integer.parseInt(fromPortTextField.getText());
            toPort = Integer.parseInt(toPortTextField.getText());
        } catch (NumberFormatException e) {
            throw new ConfigurationException("TCP Port Range inputs must me numeric");
        }
        configuration.setTcpPortRange(new Range<Integer>(fromPort, toPort));
        //selectMethodAction.setConfiguration(configuration);
    }

    @Override
    public void readConfiguration(DBStatementJdwpRunConfig configuration) {
        compileDependenciesCheckBox.setSelected(configuration.isCompileDependencies());
        fromPortTextField.setText(String.valueOf(configuration.getTcpPortRange().getFrom()));
        toPortTextField.setText(String.valueOf(configuration.getTcpPortRange().getTo()));
    }
}
