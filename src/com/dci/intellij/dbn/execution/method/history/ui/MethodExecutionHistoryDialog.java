package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MethodExecutionHistoryDialog extends DBNDialog<MethodExecutionHistoryForm> {
    private SelectAction selectAction;
    private ExecuteAction executeAction;
    private DebugAction debugAction;
    private SaveAction saveAction;
    private CloseAction closeAction;
    private boolean editable;
    private boolean debug;
    private transient MethodExecutionInput selectedExecutionInput;

    public MethodExecutionHistoryDialog(
            @NotNull Project project,
            @Nullable MethodExecutionInput executionInput,
            boolean editable,
            boolean debug) {

        super(project, "Method execution history", true);
        this.selectedExecutionInput = executionInput;
        this.editable = editable;
        this.debug = debug;
        setModal(true);
        setResizable(true);
        init();

        updateMainButtons(executionInput);
    }

    @NotNull
    @Override
    protected MethodExecutionHistoryForm createComponent() {
        return new MethodExecutionHistoryForm(this, selectedExecutionInput, debug);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        if (editable) {
            executeAction = new ExecuteAction();
            executeAction.setEnabled(false);
            debugAction = new DebugAction();
            debugAction.setEnabled(false);
            saveAction = new SaveAction();
            saveAction.setEnabled(false);
            closeAction = new CloseAction();
            return new Action[]{executeAction, debugAction, saveAction, closeAction};
        } else {
            selectAction = new SelectAction();
            selectAction.setEnabled(false);
            closeAction = new CloseAction();
            closeAction.putValue(Action.NAME, "Cancel");
            return new Action[]{selectAction, closeAction};
        }
    }

    public boolean isEditable() {
        return editable;
    }

    private void saveChanges() {
        MethodExecutionHistoryForm component = getComponent();
        component.updateMethodExecutionInputs();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(getProject());
        methodExecutionManager.setExecutionInputs(component.getExecutionInputs());
    }

    public void setSelectedExecutionInput(MethodExecutionInput selectedExecutionInput) {
        this.selectedExecutionInput = selectedExecutionInput;
    }

    public MethodExecutionInput getSelectedExecutionInput() {
        return selectedExecutionInput;
    }

    /**********************************************************
     *                         Actions                        *
     **********************************************************/
    private class SelectAction extends AbstractAction {
        SelectAction() {
            super("Select");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveChanges();
            close(OK_EXIT_CODE);
        }
    }

    private class ExecuteAction extends AbstractAction {
        ExecuteAction() {
            super("Execute", Icons.METHOD_EXECUTION_RUN);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveChanges();
            MethodExecutionInput executionInput = getComponent().getTree().getSelectedExecutionInput();
            if (executionInput != null) {
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(getProject());
                close(OK_EXIT_CODE);
                executionManager.execute(executionInput);
            }
        }
    }

    private class DebugAction extends AbstractAction {
        DebugAction() {
            super("Debug", Icons.METHOD_EXECUTION_DEBUG);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveChanges();
            MethodExecutionInput executionInput = getComponent().getTree().getSelectedExecutionInput();
            if (executionInput != null) {
                DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
                close(OK_EXIT_CODE);
                debuggerManager.startMethodDebugger(executionInput.getMethod());
            }
        }
    }

    private class SaveAction extends AbstractAction {
        SaveAction() {
            super("Save");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveChanges();
            saveAction.setEnabled(false);
            closeAction.putValue(Action.NAME, "Close");
        }
    }

    private class CloseAction extends AbstractAction {
        CloseAction() {
            super("Close");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doCancelAction();
        }
    }

    void updateMainButtons(MethodExecutionInput selection){
        if (selection == null) {
            if (executeAction != null) executeAction.setEnabled(false);
            if (debugAction != null) debugAction.setEnabled(false);
            if (selectAction != null) selectAction.setEnabled(false);
        } else {
            if (executeAction != null) executeAction.setEnabled(true);
            if (debugAction != null) debugAction.setEnabled(DatabaseFeature.DEBUGGING.isSupported(selection));
            if (selectAction != null) selectAction.setEnabled(true);
        }
    }

    void setSaveButtonEnabled(boolean enabled){
        if (!isDisposed()) {
            if (saveAction != null) saveAction.setEnabled(enabled);
            closeAction.putValue(Action.NAME, enabled ? "Cancel" : "Close");
        }
    }
}
