package com.dci.intellij.dbn.execution.common.message.ui;

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
import com.intellij.ui.IdeBorderFactory;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ExecutionMessagesPanel {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JScrollPane messagesScrollPane;
    private JPanel messagesPanel;

    private MessagesTree tMessages;

    public ExecutionMessagesPanel() {
        tMessages = new MessagesTree();
        messagesScrollPane.setViewportView(tMessages);
        messagesPanel.setBorder(IdeBorderFactory.createBorder());
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ExecutionMessages.Controls", false,
                new CloseMessagesWindowAction(tMessages),
                new ViewExecutedStatementAction(tMessages),
                new ExpandMessagesTreeAction(tMessages),
                new CollapseMessagesTreeAction(tMessages),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction(tMessages));
        actionsPanel.add(actionToolbar.getComponent());

    }

    public void addExecutionMessage(StatementExecutionMessage executionMessage, boolean focus) {
        tMessages.addExecutionMessage(executionMessage, focus);
    }

    public void addCompilerMessage(CompilerMessage compilerMessage, boolean focus) {
        tMessages.addCompilerMessage(compilerMessage, focus);
    }

    public void reset() {
        tMessages.reset();
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void select(CompilerMessage compilerMessage) {
        tMessages.selectCompilerMessage(compilerMessage);
    }
}
