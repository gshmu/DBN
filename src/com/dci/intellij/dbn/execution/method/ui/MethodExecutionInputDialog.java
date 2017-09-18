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
    private DBDebuggerType debuggerType;

    public MethodExecutionInputDialog(MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType) {
        super(executionInput.getProject(), (debuggerType.isDebug() ? "Debug" : "Execute") + " method", true);
        this.debuggerType = debuggerType;
        setModal(true);
        setResizable(true);
        component = new MethodExecutionInputForm(this, executionInput, true, debuggerType);
        init();
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

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private class ExecuteAction extends AbstractAction {
        public ExecuteAction() {
            super(debuggerType.isDebug() ? "Debug" : "Execute",
                    debuggerType.isDebug() ? Icons.METHOD_EXECUTION_DEBUG : Icons.METHOD_EXECUTION_RUN);
            putValue(FOCUSED_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            component.updateExecutionInput();
            doOKAction();
        }
    }
}
