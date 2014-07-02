package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class EditObjectDataAction extends AnAction {
    private DBSchemaObject object;
    public EditObjectDataAction(DBSchemaObject object) {
        super("Edit data", null, Icons.OBEJCT_EDIT_DATA);
        this.object = object;
        setDefaultIcon(true);
    }

    public void actionPerformed(AnActionEvent e) {
        DatabaseFileSystem.getInstance().openEditor(object);
    }
}