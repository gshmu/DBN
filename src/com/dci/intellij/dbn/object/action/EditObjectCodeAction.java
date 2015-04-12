package com.dci.intellij.dbn.object.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class EditObjectCodeAction extends AnAction {
    private DBObjectRef<DBSchemaObject> objectRef;
    public EditObjectCodeAction(DBSchemaObject object) {
        super("Edit Code", null, Icons.OBEJCT_EDIT_SOURCE);
        objectRef = DBObjectRef.from(object);
        setDefaultIcon(true);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSchemaObject schemaObject = DBObjectRef.getnn(objectRef);
        DatabaseFileSystem fileSystem = DatabaseFileSystem.getInstance();
        fileSystem.openEditor(schemaObject, EditorProviderId.CODE, true);
    }
}
