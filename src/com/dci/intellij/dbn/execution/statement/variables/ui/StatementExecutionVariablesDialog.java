package com.dci.intellij.dbn.execution.statement.variables.ui;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;

public class StatementExecutionVariablesDialog extends DBNDialog {
    private StatementExecutionVariablesForm variablesForm;
    private StatementExecutionVariablesBundle variablesBundle;

    public StatementExecutionVariablesDialog(Project project, StatementExecutionVariablesBundle variablesBundle, String statementText) {
        super(project, "Execution Variables", true);
        this.variablesBundle = variablesBundle;
        setModal(true);
        setResizable(true);
        variablesForm = new StatementExecutionVariablesForm(variablesBundle, statementText);
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.ExecutionVariables";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return variablesForm.getPreferredFocusComponent();
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
            if (variablesBundle.isIncomplete()) {
                Messages.showErrorDialog(
                        "You didn't specify values for all the variables. \n" +
                        "Please enter values for all the listed variables and try again.",
                        Constants.DBN_TITLE_PREFIX + "Statement execution");
            } else if (variablesBundle.hasErrors()) {
                Messages.showErrorDialog(
                        "You provided invalid/unsupported variable values. \n" +
                        "Please correct your input and try again.",
                        Constants.DBN_TITLE_PREFIX + "Statement execution");
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
