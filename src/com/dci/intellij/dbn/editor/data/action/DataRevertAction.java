package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class DataRevertAction extends AnAction{
    private DatasetEditorModelCell cell;

    public DataRevertAction(DatasetEditorModelCell cell) {
        super("Revert Changes", null, Icons.ACTION_REVERT_CHANGES);
        this.cell = cell;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        cell.revertChanges();
    }


}
