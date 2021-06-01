package com.dci.intellij.dbn.code.common.completion.options.sorting.action;

import com.dci.intellij.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;
import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.ListUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MoveDownAction extends AnAction {
    private final JList list;
    private final CodeCompletionSortingSettings settings;
    public MoveDownAction(JList list, CodeCompletionSortingSettings settings) {
        super("Move Down", null, Icons.ACTION_MOVE_DOWN);
        this.list = list;
        this.settings = settings;
    }

    @Override
    public void update(AnActionEvent e) {
        int[] indices = list.getSelectedIndices();
        boolean enabled =
                list.isEnabled() &&
                indices.length > 0 &&
                indices[indices.length-1] < list.getModel().getSize() -1;
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ListUtil.moveSelectedItemsDown(list);
        settings.setModified(true);
    }
}