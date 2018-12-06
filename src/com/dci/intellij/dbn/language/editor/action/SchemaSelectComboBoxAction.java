package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
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

import static com.dci.intellij.dbn.common.util.ActionUtil.getProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class SchemaSelectComboBoxAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "Schema";

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        Project project = getProject(component);
        VirtualFile virtualFile = getVirtualFile(component);
        if (virtualFile != null) {
            ConnectionHandler activeConnection = FileConnectionMappingManager.getInstance(project).getConnectionHandler(virtualFile);
            if (activeConnection != null && !activeConnection.isVirtual() && !activeConnection.isDisposed()) {
                for (DBSchema schema : activeConnection.getObjectBundle().getSchemas()){
                    actionGroup.add(new SchemaSelectAction(schema));
                }
            }
        }
        return actionGroup;
    }

    public void update(@NotNull AnActionEvent e) {
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        String text = NAME;

        Icon icon = null;
        boolean visible = false;
        boolean enabled = true;

        if (project != null && virtualFile != null) {
            FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = mappingManager.getConnectionHandler(virtualFile);
            visible = activeConnection != null && !activeConnection.isVirtual();
            if (visible) {
                DBSchema schema = mappingManager.getDatabaseSchema(virtualFile);
                if (schema != null) {
                    text = schema.getName();
                    icon = schema.getIcon();
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
