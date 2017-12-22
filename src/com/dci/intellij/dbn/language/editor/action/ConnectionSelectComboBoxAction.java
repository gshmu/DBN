package com.dci.intellij.dbn.language.editor.action;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ConnectionSelectComboBoxAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "DB Connections";

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        Project project = ActionUtil.getProject(component);
        return new ConnectionSelectActionGroup(project);
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        String text = NAME;
        Icon icon = null;

        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getConnectionHandler(virtualFile);
            if (activeConnection != null) {
                text = activeConnection.getQualifiedName();
                icon = activeConnection.getIcon();
            }

            boolean isConsole = virtualFile instanceof DBConsoleVirtualFile;
            presentation.setVisible(!isConsole);

            if (virtualFile.isInLocalFileSystem()) {
                DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                DBSchemaObject editableObject = fileAttachmentManager.getEditableObject(virtualFile);
                if (editableObject != null) {
                    boolean isOpened = DatabaseFileSystem.isFileOpened(editableObject);
                    presentation.setEnabled(!isOpened);
                }
            }
        }

        presentation.setText(text, false);
        presentation.setIcon(icon);
    }
 }
