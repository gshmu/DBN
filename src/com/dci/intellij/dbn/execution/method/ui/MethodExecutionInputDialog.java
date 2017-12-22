package com.dci.intellij.dbn.execution.method.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;

public class MethodExecutionInputDialog extends DBNDialog<MethodExecutionInputForm> {
    private MethodExecutionInput executionInput;
    private DBDebuggerType debuggerType;

    public MethodExecutionInputDialog(@NotNull MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType) {
        super(executionInput.getProject(), (debuggerType.isDebug() ? "Debug" : "Execute") + " method", true);
        this.executionInput = executionInput;
        this.debuggerType = debuggerType;
        setModal(true);
        setResizable(true);
        init();
    }

    @NotNull
    @Override
    protected MethodExecutionInputForm createComponent() {
        return new MethodExecutionInputForm(this, executionInput, true, debuggerType);
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new ExecuteAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    private class ExecuteAction extends AbstractAction {
        ExecuteAction() {
            super(debuggerType.isDebug() ? "Debug" : "Execute",
                    debuggerType.isDebug() ? Icons.METHOD_EXECUTION_DEBUG : Icons.METHOD_EXECUTION_RUN);
            putValue(FOCUSED_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                getComponent().updateExecutionInput();
            } finally {
                doOKAction();
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        executionInput = null;
    }
}
