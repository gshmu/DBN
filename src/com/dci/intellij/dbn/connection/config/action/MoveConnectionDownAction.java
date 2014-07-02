package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.ListUtil;

import javax.swing.JList;

public class MoveConnectionDownAction extends DumbAwareAction {
    private JList list;
    private ConnectionBundle connectionBundle;

    public MoveConnectionDownAction(JList list, ConnectionBundle connectionBundle) {
        super("Move selection down", null, Icons.ACTION_MOVE_DOWN);
        this.list = list;
        this.connectionBundle = connectionBundle;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionBundle.setModified(true);
        ListUtil.moveSelectedItemsDown(list);
    }

    public void update(AnActionEvent e) {
        int length = list.getSelectedValues().length;
        boolean enabled = length > 0 && list.getMaxSelectionIndex() < list.getModel().getSize() - 1;
        e.getPresentation().setEnabled(enabled);
    }
}
