package com.dci.intellij.dbn.editor.data.state.sorting.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.editor.data.state.sorting.ui.DatasetSortingColumnForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.Icon;

public class ChangeSortingDirectionAction extends DumbAwareAction {
    private DatasetSortingColumnForm form;

    public ChangeSortingDirectionAction(DatasetSortingColumnForm form) {
        this.form = form;
    }

    public void update(AnActionEvent e) {
        SortingInstruction sortingInstruction = form.getSortingInstruction();
        SortDirection direction = sortingInstruction.getDirection();
        Icon icon =
            direction == SortDirection.ASCENDING ? Icons.DATA_SORTING_ASC :
            direction == SortDirection.DESCENDING ? Icons.DATA_SORTING_DESC : null;
        e.getPresentation().setIcon(icon);
        e.getPresentation().setText("Change sorting direction");
    }

    public void actionPerformed(AnActionEvent e) {
        form.getSortingInstruction().switchDirection();
    }

}
