package com.dci.intellij.dbn.execution.script.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.script.ScriptExecutionInput;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ScriptExecutionInputDialog extends DBNDialog<ScriptExecutionInputForm> {
    private ScriptExecutionInput executionInput;

    public ScriptExecutionInputDialog(Project project, ScriptExecutionInput executionInput) {
        super(project, "Execute SQL script", true);
        this.executionInput = executionInput;
        setModal(true);
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "Execute");
        init();
    }

    @NotNull
    @Override
    protected ScriptExecutionInputForm createForm() {
        return new ScriptExecutionInputForm(this, executionInput);
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @Override
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
}
