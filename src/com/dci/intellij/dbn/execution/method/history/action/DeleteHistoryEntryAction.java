package com.dci.intellij.dbn.execution.method.history.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.history.ui.MethodExecutionHistoryTree;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class DeleteHistoryEntryAction extends DumbAwareAction {
    MethodExecutionHistoryTree tree;

    public DeleteHistoryEntryAction(MethodExecutionHistoryTree tree) {
        super("Delete", null, Icons.ACTION_REMOVE);
        this.tree = tree;
    }

    public void actionPerformed(AnActionEvent e) {
        tree.removeSelectedEntries();
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(!tree.isSelectionEmpty());
    }
}
