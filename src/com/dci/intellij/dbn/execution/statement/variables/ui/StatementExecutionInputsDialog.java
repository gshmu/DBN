package com.dci.intellij.dbn.execution.statement.variables.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.intellij.openapi.project.Project;

public class StatementExecutionInputsDialog extends DBNDialog<StatementExecutionInputForm> {
    private StatementExecutionProcessor executionProcessor;
    private ExecuteAction executeAction;
    private DBDebuggerType debuggerType;
    private boolean reuseVariables = false;

    public StatementExecutionInputsDialog(StatementExecutionProcessor executionProcessor, String statementText, DBDebuggerType debuggerType, boolean isBulkExecution) {
        super(executionProcessor.getProject(), (debuggerType.isDebug() ? "Debug" : "Execute") + " Statement", true);
        this.executionProcessor = executionProcessor;
        this.debuggerType = debuggerType;
        setModal(true);
        setResizable(true);
        executeAction = new ExecuteAction();
        component = new StatementExecutionInputForm(this, executionProcessor, statementText, debuggerType, isBulkExecution);
        init();
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                executeAction,
                getCancelAction(),
                getHelpAction()
        };
    }

    public void setActionEnabled(boolean enabled) {
        executeAction.setEnabled(enabled);
    }

    private class ExecuteAction extends AbstractAction {
        public ExecuteAction() {
            super(debuggerType.isDebug() ? "Debug" : "Execute", debuggerType.isDebug() ? Icons.STMT_EXECUTION_DEBUG : Icons.STMT_EXECUTION_RUN);
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            component.updateExecutionInput();
            StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
            Project project = getProject();
            if (executionVariables != null) {
                if (!executionVariables.isProvided()) {
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
            } else {
                doOKAction();
            }
        }
    }

    public boolean isReuseVariables() {
        return reuseVariables;
    }

    public void setReuseVariables(boolean reuseVariables) {
        this.reuseVariables = reuseVariables;
    }

    @Override
    public void dispose() {
        super.dispose();
        executionProcessor = null;
    }


}
