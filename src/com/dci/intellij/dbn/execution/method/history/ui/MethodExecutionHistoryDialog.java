package com.dci.intellij.dbn.execution.method.history.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

public class MethodExecutionHistoryDialog extends DBNDialog implements Disposable {
    private MethodExecutionHistoryForm mainComponent;
    private SelectAction selectAction;
    private ExecuteAction executeAction;
    private SaveAction saveAction;
    private CloseAction closeAction;
    private boolean select;
    private MethodExecutionInput selectedExecutionInput;

    public MethodExecutionHistoryDialog(Project project, MethodExecutionHistory executionHistory, MethodExecutionInput selectedExecutionInput, boolean select) {
        super(project, "Method Execution History", true);
        this.select = select;
        setModal(true);
        setResizable(true);
        mainComponent = new MethodExecutionHistoryForm(this, executionHistory);
        if (selectedExecutionInput == null) {
            selectedExecutionInput = executionHistory.getLastSelection();
        }

        if (selectedExecutionInput != null) {
            showMethodExecutionPanel(selectedExecutionInput);
            this.selectedExecutionInput = selectedExecutionInput;
            mainComponent.setSelectedInput(selectedExecutionInput);
        }
        init();
        setMainButtonEnabled(selectedExecutionInput != null);
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.MethodExecutionHistory";
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return mainComponent.getComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        if (select) {
            executeAction = new ExecuteAction();
            executeAction.setEnabled(false);
            saveAction = new SaveAction();
            saveAction.setEnabled(false);
            closeAction = new CloseAction();
            return new Action[]{executeAction, saveAction, closeAction};
        } else {
            selectAction = new SelectAction();
            selectAction.setEnabled(false);
            closeAction = new CloseAction();
            closeAction.putValue(Action.NAME, "Cancel");
            return new Action[]{selectAction, closeAction};
        }
    }

    private void saveChanges() {
        mainComponent.updateMethodExecutionInputs();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(getProject());
        methodExecutionManager.setExecutionInputs(mainComponent.getExecutionInputs());
    }

    public void dispose() {
        super.dispose();
        mainComponent.dispose();
        mainComponent = null;
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
        public SelectAction() {
            super("Select");
        }

        public void actionPerformed(ActionEvent e) {
            saveChanges();
            close(OK_EXIT_CODE);
        }
    }

    private class ExecuteAction extends AbstractAction {
        public ExecuteAction() {
            super("Execute", Icons.METHOD_EXECUTION_RUN);
        }

        public void actionPerformed(ActionEvent e) {
            saveChanges();
            MethodExecutionInput executionInput = mainComponent.getTree().getSelectedExecutionInput();
            if (executionInput != null) {
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(getProject());
                close(OK_EXIT_CODE);
                executionManager.execute(executionInput);
            }
        }
    }

    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save");
        }

        public void actionPerformed(ActionEvent e) {
            saveChanges();
            saveAction.setEnabled(false);
            closeAction.putValue(Action.NAME, "Close");
        }
    }

    private class CloseAction extends AbstractAction {
        public CloseAction() {
            super("Close");
        }

        public void actionPerformed(ActionEvent e) {
            doCancelAction();
        }
    }

    public void setMainButtonEnabled(boolean enabled){
        if (executeAction != null) executeAction.setEnabled(enabled);
        if (selectAction != null) selectAction.setEnabled(enabled);
    }

    public void setSaveButtonEnabled(boolean enabled){
        if (!isDisposed()) {
            if (saveAction != null) saveAction.setEnabled(enabled);
            closeAction.putValue(Action.NAME, enabled ? "Cancel" : "Close");
        }
    }

    public void showMethodExecutionPanel(MethodExecutionInput executionInput){
        mainComponent.showMethodExecutionPanel(executionInput);
    }
}
