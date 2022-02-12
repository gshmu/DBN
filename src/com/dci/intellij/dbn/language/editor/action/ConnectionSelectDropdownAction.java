package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectionSelectDropdownAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "DB Connections";

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        Project project = Lookup.getProject(component);
        return new ConnectionSelectActionGroup(project);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        String text = NAME;
        Icon icon = null;

        Project project = Lookup.getProject(e);
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getConnection(virtualFile);
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
