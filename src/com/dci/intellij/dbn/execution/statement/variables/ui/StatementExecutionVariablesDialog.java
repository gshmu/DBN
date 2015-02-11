package com.dci.intellij.dbn.execution.statement.variables.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.intellij.openapi.project.Project;

public class StatementExecutionVariablesDialog extends DBNDialog {
    private StatementExecutionVariablesForm variablesForm;
    private StatementExecutionProcessor executionProcessor;

    public StatementExecutionVariablesDialog(StatementExecutionProcessor executionProcessor, String statementText) {
        super(executionProcessor.getProject(), "Execution Variables", true);
        this.executionProcessor = executionProcessor;
        setModal(true);
        setResizable(true);
        variablesForm = new StatementExecutionVariablesForm(executionProcessor, statementText);
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.ExecutionVariables";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return variablesForm.getPreferredFocusedComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new ExecuteAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    private class ExecuteAction extends AbstractAction {
        public ExecuteAction() {
            super("Execute", Icons.STMT_EXECUTION_RUN);
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            variablesForm.saveValues();
            StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
            Project project = getProject();
            if (executionVariables.isIncomplete()) {
                MessageUtil.showErrorDialog(
                        project,
                        "Statement execution",
                        "You didn't specify values for all the variables. \n" +
                            "Please enter values for all the listed variables and try again."
                );
            } else if (executionVariables.hasErrors()) {
                MessageUtil.showErrorDialog(
                        project,
                        "Statement execution",
                        "You provided invalid/unsupported variable values. \n" +
                            "Please correct your input and try again."
                );
            } else {
                doOKAction();
            }
        }
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return variablesForm.getComponent();
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            variablesForm.dispose();
        }
    }


}
