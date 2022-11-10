package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class ObjectEditCodeAction extends DumbAwareAction {
    private DBObjectRef<DBSchemaObject> objectRef;
    ObjectEditCodeAction(DBSchemaObject object) {
        super("Edit Code", null, Icons.OBEJCT_EDIT_SOURCE);
        objectRef = DBObjectRef.of(object);
        setDefaultIcon(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSchemaObject schemaObject = DBObjectRef.ensure(objectRef);
        DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
        databaseFileSystem.connectAndOpenEditor(schemaObject, EditorProviderId.CODE, false, true);
    }
}
