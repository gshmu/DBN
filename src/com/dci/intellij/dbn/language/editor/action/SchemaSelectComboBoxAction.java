package com.dci.intellij.dbn.language.editor.action;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SchemaSelectComboBoxAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "Schema";

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        Project project = ActionUtil.getProject(component);
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(DataManager.getInstance().getDataContext(component));
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

    public void update(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
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
