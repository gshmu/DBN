package com.dci.intellij.dbn.ddl.ui;

import com.dci.intellij.dbn.common.editor.EditorNotificationPanel;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DDLMappedNotificationPanel extends EditorNotificationPanel {

    public DDLMappedNotificationPanel(@NotNull final VirtualFile virtualFile, final DBSchemaObject editableObject) {
        super(MessageType.INFO);
        final Project project = editableObject.getProject();
        final DBObjectRef<DBSchemaObject> editableObjectRef = DBObjectRef.from(editableObject);
        setText("This DDL file is attached to the database " + editableObject.getQualifiedNameWithType() + ". Changes done to the " + editableObject.getObjectType().getName() + " are mirrored to this DDL file, overwriting any changes you may do to it.");
        createActionLabel("Detach", new Runnable() {
            @Override
            public void run() {
                if (!project.isDisposed()) {
                    DDLFileAttachmentManager attachmentManager = DDLFileAttachmentManager.getInstance(project);
                    attachmentManager.detachDDLFile(virtualFile);
                    DBSchemaObject editableObject = DBObjectRef.get(editableObjectRef);
                    if (editableObject != null) {
                        DatabaseFileSystem.getInstance().reopenEditor(editableObject);
                    }
                }
            }
        });
    }
}
