package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class EditObjectCodeAction extends AnAction {
    private DBSchemaObject object;
    public EditObjectCodeAction(DBSchemaObject object) {
        super("Edit code", null, Icons.OBEJCT_EDIT_SOURCE);
        this.object = object;
        setDefaultIcon(true);
    }

    public void actionPerformed(AnActionEvent e) {
        DatabaseFileSystem.getInstance().openEditor(object);
    }
}
