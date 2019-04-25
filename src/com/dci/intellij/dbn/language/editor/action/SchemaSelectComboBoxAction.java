package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SchemaSelectComboBoxAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "Schema";

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        Project project = Lookup.getProject(component);
        VirtualFile virtualFile = Lookup.getVirtualFile(component);
        if (virtualFile != null) {
            ConnectionHandler activeConnection = FileConnectionMappingManager.getInstance(project).getConnectionHandler(virtualFile);
            if (Failsafe.check(activeConnection) && !activeConnection.isVirtual()) {
                for (DBSchema schema : activeConnection.getObjectBundle().getSchemas()){
                    actionGroup.add(new SchemaSelectAction(schema));
                }
            }
        }
        return actionGroup;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = Lookup.getProject(e);
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        String text = NAME;

        Icon icon = null;
        boolean visible = false;
        boolean enabled = true;

        if (project != null && virtualFile != null) {
            FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = mappingManager.getConnectionHandler(virtualFile);
            visible = activeConnection != null && !activeConnection.isVirtual();
            if (visible) {
                SchemaId schema = mappingManager.getDatabaseSchema(virtualFile);
                if (schema != null) {
                    text = schema.getName();
                    icon = Icons.DBO_SCHEMA;
                    enabled = true;
                }

                if (virtualFile.isInLocalFileSystem()) {
                    DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                    DBSchemaObject editableObject = fileAttachmentManager.getEditableObject(virtualFile);
                    if (editableObject != null) {
                        boolean isOpened = DatabaseFileSystem.isFileOpened(editableObject);
                        if (isOpened) {
                            enabled = false;
                        }
                    }
                }
            }
        }

        Presentation presentation = e.getPresentation();
        presentation.setText(text, false);
        presentation.setDescription("Select current schema");
        presentation.setIcon(icon);
        presentation.setVisible(visible);
        presentation.setEnabled(enabled);
    }
 }
