package com.dci.intellij.dbn.connection.mapping.ui;

import com.dci.intellij.dbn.common.editor.EditorNotificationPanel;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContext;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class FileConnectionContextNotificationPanel extends EditorNotificationPanel {

    public FileConnectionContextNotificationPanel(
            @NotNull Project project,
            @NotNull VirtualFile file,
            @NotNull FileConnectionContext mapping) {
        super(MessageType.SYSTEM);

        ConnectionId connectionId = mapping.getConnectionId();
        ConnectionHandler connection = ConnectionCache.resolveConnection(connectionId);
        if (connection != null) {
            setText(connection.getPresentableText());
            setIcon(connection.getIcon());
        } else {
            setText("No connection selected");
            setIcon(null);
        }

        createActionLabel("Unlink", () -> {
                FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
                contextManager.removeMapping(file);
            });


/*
        Project project = editableObject.getProject();
        DBObjectRef<DBSchemaObject> editableObjectRef = DBObjectRef.of(editableObject);
        String objectName = editableObject.getQualifiedNameWithType();
        String objectTypeName = editableObject.getObjectType().getName();
        setText("This DDL file is attached to the database " + objectName + ". " +
                "Changes done to the " + objectTypeName + " are mirrored to this DDL file, overwriting any changes you may do to it.");
        createActionLabel("Detach", () -> {
            if (!project.isDisposed()) {
                DDLFileAttachmentManager attachmentManager = DDLFileAttachmentManager.getInstance(project);
                attachmentManager.detachDDLFile(virtualFile);
                DBSchemaObject editableObject1 = DBObjectRef.get(editableObjectRef);
                if (editableObject1 != null) {
                    DatabaseFileSystem.getInstance().reopenEditor(editableObject1);
                }
            }
        });
*/
    }
}
