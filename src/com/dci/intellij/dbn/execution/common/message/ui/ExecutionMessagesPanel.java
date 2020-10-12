package com.dci.intellij.dbn.execution.common.message.ui;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.common.message.action.ExecutedStatementViewAction;
import com.dci.intellij.dbn.execution.common.message.action.ExecutionEngineSettingsAction;
import com.dci.intellij.dbn.execution.common.message.action.MessagesTreeCollapseAction;
import com.dci.intellij.dbn.execution.common.message.action.MessagesTreeExpandAction;
import com.dci.intellij.dbn.execution.common.message.action.MessagesWindowCloseAction;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.dci.intellij.dbn.execution.common.ui.ExecutionConsoleForm;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;

public class ExecutionMessagesPanel extends DBNFormImpl{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JScrollPane messagesScrollPane;
    private JPanel messagesPanel;

    private final MessagesTree messagesTree;

    public ExecutionMessagesPanel(ExecutionConsoleForm parent) {
        super(parent);
        messagesTree = new MessagesTree(this);
        messagesScrollPane.setViewportView(messagesTree);
        messagesPanel.setBorder(IdeBorderFactory.createBorder());
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ExecutionMessages.Controls", false,
                new MessagesWindowCloseAction(messagesTree),
                new ExecutedStatementViewAction(messagesTree),
                new MessagesTreeExpandAction(messagesTree),
                new MessagesTreeCollapseAction(messagesTree),
                ActionUtil.SEPARATOR,
                new ExecutionEngineSettingsAction(messagesTree));
        actionsPanel.add(actionToolbar.getComponent());
    }

    public void resetMessagesStatus() {
        getMessagesTree().resetMessagesStatus();
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage, boolean select, boolean focus) {
        return getMessagesTree().addExecutionMessage(executionMessage, select, focus);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage, boolean select) {
        return getMessagesTree().addCompilerMessage(compilerMessage, select);
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage, boolean select) {
        return getMessagesTree().addExplainPlanMessage(explainPlanMessage, select);
    }

    public void reset() {
        getMessagesTree().reset();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public void selectMessage(@NotNull CompilerMessage compilerMessage) {
        getMessagesTree().selectCompilerMessage(compilerMessage);
    }

    public void selectMessage(@NotNull StatementExecutionMessage statementExecutionMessage, boolean focus) {
        getMessagesTree().selectExecutionMessage(statementExecutionMessage, focus);
    }

    public void expand(TreePath treePath) {
        getMessagesTree().makeVisible(treePath);
    }

    @NotNull
    public MessagesTree getMessagesTree() {
        return Failsafe.nn(messagesTree);
    }
}
