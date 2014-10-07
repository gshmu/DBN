package com.dci.intellij.dbn.execution.common.message.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.common.message.action.CloseMessagesWindowAction;
import com.dci.intellij.dbn.execution.common.message.action.CollapseMessagesTreeAction;
import com.dci.intellij.dbn.execution.common.message.action.ExpandMessagesTreeAction;
import com.dci.intellij.dbn.execution.common.message.action.OpenSettingsAction;
import com.dci.intellij.dbn.execution.common.message.action.ViewExecutedStatementAction;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

public class ExecutionMessagesPanel extends DBNFormImpl{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JScrollPane messagesScrollPane;
    private JPanel messagesPanel;

    private MessagesTree messagesTree;

    public ExecutionMessagesPanel(Project project) {
        messagesTree = new MessagesTree(project);
        messagesScrollPane.setViewportView(messagesTree);
        messagesPanel.setBorder(IdeBorderFactory.createBorder());
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ExecutionMessages.Controls", false,
                new CloseMessagesWindowAction(messagesTree),
                new ViewExecutedStatementAction(messagesTree),
                new ExpandMessagesTreeAction(messagesTree),
                new CollapseMessagesTreeAction(messagesTree),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction(messagesTree));
        actionsPanel.add(actionToolbar.getComponent());

        Disposer.register(this, messagesTree);
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage, boolean focus) {
        return messagesTree.addExecutionMessage(executionMessage, focus);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage, boolean focus) {
        return messagesTree.addCompilerMessage(compilerMessage, focus);
    }

    public void reset() {
        messagesTree.reset();
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void select(@NotNull CompilerMessage compilerMessage) {
        messagesTree.focus(compilerMessage);
    }

    public void select(@NotNull StatementExecutionMessage statementExecutionMessage) {
        messagesTree.focus(statementExecutionMessage);
    }

    public void expand(TreePath treePath) {
        messagesTree.makeVisible(treePath);
    }

    public TreePath getMessageTreePath(StatementExecutionMessage executionMessage) {
        return null;
    }
}
