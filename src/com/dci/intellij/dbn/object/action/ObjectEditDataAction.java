package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class ObjectEditDataAction extends DumbAwareAction {
    private DBObjectRef<DBSchemaObject> objectRef;
    public ObjectEditDataAction(DBSchemaObject object) {
        super("Edit Data", null, Icons.OBEJCT_EDIT_DATA);
        objectRef = DBObjectRef.from(object);
        setDefaultIcon(true);
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.ensure(objectRef);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSchemaObject object = getObject();
        DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
        databaseFileSystem.connectAndOpenEditor(object, EditorProviderId.DATA, false, true);
    }
}