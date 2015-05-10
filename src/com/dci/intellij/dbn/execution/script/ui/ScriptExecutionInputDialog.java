package com.dci.intellij.dbn.execution.script.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.script.CmdLineExecutionInput;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import javax.swing.JComponent;

public class ScriptExecutionInputDialog extends DBNDialog<ScriptExecutionInputForm> {
    private CmdLineExecutionInput executionInput = new CmdLineExecutionInput();

    public ScriptExecutionInputDialog(Project project, CmdLineExecutionInput executionInput) {
        super(project, "Execute SQL Script", true);
        this.executionInput = executionInput;
        setModal(true);
        component = new ScriptExecutionInputForm(this, executionInput);
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "Execute");
        init();
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component == null ? null : component.getPreferredFocusedComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
        };
    }

    public void setActionEnabled(boolean enabled) {
        getOKAction().setEnabled(enabled);
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
