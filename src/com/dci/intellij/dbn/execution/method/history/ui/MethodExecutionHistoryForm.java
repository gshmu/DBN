package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionInputForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodExecutionHistoryForm extends DBNFormImpl<MethodExecutionHistoryDialog> {
    private JPanel mainPanel;
    private JTree executionInputsTree;
    private JPanel actionsPanel;
    private JPanel argumentsPanel;
    private JPanel contentPanel;
    private MethodExecutionHistory executionHistory;
    private ChangeListener changeListener;
    private boolean debug;

    private Map<MethodExecutionInput, MethodExecutionInputForm> methodExecutionForms;

    MethodExecutionHistoryForm(MethodExecutionHistoryDialog parentComponent, MethodExecutionHistory executionHistory, boolean debug) {
        super(parentComponent);
        this.executionHistory = executionHistory;
        this.debug = debug;
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new ShowGroupedTreeAction(),
                new DeleteHistoryEntryAction(),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction());
        actionsPanel.add(actionToolbar.getComponent());
        methodExecutionForms = new HashMap<>();
        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(contentPanel);
        JBSplitter splitter = (JBSplitter) contentPanel.getComponent(0);
        splitter.setProportion((float) 0.32);
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    List<MethodExecutionInput> getExecutionInputs() {
        MethodExecutionHistoryTreeModel model = (MethodExecutionHistoryTreeModel) executionInputsTree.getModel();
        return model.getExecutionInputs();
    }

    private void createUIComponents() {
        boolean group = executionHistory.isGroupEntries();
        executionInputsTree = new MethodExecutionHistoryTree(getParentComponent(), executionHistory, group, debug);
        Disposer.register(this, (Disposable) executionInputsTree);
    }

    public MethodExecutionHistoryTree getTree() {
        return (MethodExecutionHistoryTree) executionInputsTree;
    }

    @Override
    public void dispose() {
        super.dispose();
        executionInputsTree = null;
        executionHistory = null;
    }

    void showMethodExecutionPanel(MethodExecutionInput executionInput) {
        argumentsPanel.removeAll();
        if (executionInput != null &&
                !executionInput.isObsolete() &&
                !executionInput.isInactive()) {
            MethodExecutionInputForm methodExecutionInputForm = methodExecutionForms.get(executionInput);
            if (methodExecutionInputForm == null) {
                methodExecutionInputForm = new MethodExecutionInputForm(this, executionInput, true, DBDebuggerType.NONE);
                methodExecutionInputForm.addChangeListener(getChangeListener());
                methodExecutionForms.put(executionInput, methodExecutionInputForm);
            }
            argumentsPanel.add(methodExecutionInputForm.getComponent(), BorderLayout.CENTER);
        }

        GUIUtil.repaint(argumentsPanel);
    }

    private ChangeListener getChangeListener() {
        if (changeListener == null) {
            changeListener = e -> getParentComponent().setSaveButtonEnabled(true);
        }
        return changeListener;
    }

    void updateMethodExecutionInputs() {
        for (MethodExecutionInputForm methodExecutionComponent : methodExecutionForms.values()) {
            methodExecutionComponent.updateExecutionInput();
        }
    }

    void setSelectedInput(MethodExecutionInput selectedExecutionInput) {
        getTree().setSelectedInput(selectedExecutionInput);
    }

    public class DeleteHistoryEntryAction extends DumbAwareAction {
        DeleteHistoryEntryAction() {
            super("Delete", null, Icons.ACTION_REMOVE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            getTree().removeSelectedEntries();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(!getTree().isSelectionEmpty());
            e.getPresentation().setVisible(getParentComponent().isEditable());
        }
    }

    public static class OpenSettingsAction extends DumbAwareAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.ensureProject(e);
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            settingsManager.openProjectSettings(ConfigId.EXECUTION_ENGINE);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setIcon(Icons.ACTION_SETTINGS);
            presentation.setText("Settings");
        }
    }

    public class ShowGroupedTreeAction extends ToggleAction {
        ShowGroupedTreeAction() {
            super("Group by Program", "Show grouped by program", Icons.ACTION_GROUP);
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return getTree().isGrouped();
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            getTemplatePresentation().setText(state ? "Ungroup" : "Group by Program");
            getTree().showGrouped(state);
            Project project = ActionUtil.getProject(e);
            if (project != null) {
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                executionManager.getExecutionHistory().setGroupEntries(state);
            }
        }
    }
}
