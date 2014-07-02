package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.history.action.DeleteHistoryEntryAction;
import com.dci.intellij.dbn.execution.method.history.action.OpenSettingsAction;
import com.dci.intellij.dbn.execution.method.history.action.ShowGroupedTreeAction;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionForm;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.GuiUtils;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodExecutionHistoryForm extends DBNFormImpl implements DBNForm {
    private JPanel mainPanel;
    private JTree executionInputsTree;
    private JPanel actionsPanel;
    private JPanel argumentsPanel;
    private List<MethodExecutionInput> executionInputs;
    private MethodExecutionHistoryDialog dialog;
    private ChangeListener changeListener;

    private Map<MethodExecutionInput, MethodExecutionForm> methodExecutionForms;

    public MethodExecutionHistoryForm(MethodExecutionHistoryDialog dialog, List<MethodExecutionInput> executionInputs) {
        this.dialog = dialog;
        this.executionInputs = executionInputs;
        MethodExecutionHistoryTree tree = getTree();
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new ShowGroupedTreeAction(tree),
                new DeleteHistoryEntryAction(tree),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction());
        actionsPanel.add(actionToolbar.getComponent());
        methodExecutionForms = new HashMap<MethodExecutionInput, MethodExecutionForm>();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public List<MethodExecutionInput> getExecutionInputs() {
        MethodExecutionHistoryTreeModel model = (MethodExecutionHistoryTreeModel) executionInputsTree.getModel();
        return model.getExecutionInputs();
    }

    private void createUIComponents() {
        boolean group = MethodExecutionManager.getInstance(dialog.getProject()).isGroupHistoryEntries();
        executionInputsTree = new MethodExecutionHistoryTree(dialog, executionInputs, group);
    }

    public MethodExecutionHistoryTree getTree() {
        return (MethodExecutionHistoryTree) executionInputsTree;
    }

    public void dispose() {
        super.dispose();
        getTree().dispose();
        executionInputsTree = null;
        executionInputs = null;
        dialog = null;
    }

    public void showMethodExecutionPanel(MethodExecutionInput executionInput) {
        argumentsPanel.removeAll();
        if (executionInput != null && !executionInput.isObsolete()) {
            MethodExecutionForm methodExecutionForm = methodExecutionForms.get(executionInput);
            if (methodExecutionForm == null) {
                methodExecutionForm = new MethodExecutionForm(executionInput, true, false);
                methodExecutionForm.addChangeListener(getChangeListener());
                methodExecutionForms.put(executionInput, methodExecutionForm);
            }
            argumentsPanel.add(methodExecutionForm.getComponent(), BorderLayout.CENTER);
        }
        argumentsPanel.updateUI();
    }

    private ChangeListener getChangeListener() {
        if (changeListener == null) {
            changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    dialog.setSaveButtonEnabled(true);
                }
            };
        }
        return changeListener;
    }

    public void updateMethodExecutionInputs() {
        for (MethodExecutionForm methodExecutionComponent : methodExecutionForms.values()) {
            methodExecutionComponent.updateExecutionInput();
        }
    }

    public void setSelectedInput(MethodExecutionInput selectedExecutionInput) {
        getTree().setSelectedInput(selectedExecutionInput);
    }
}
