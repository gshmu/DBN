package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.ActionUtil.*;

public class ConnectionSelectAction extends DumbAwareAction {
    private final ConnectionHandler connectionHandler;

    ConnectionSelectAction(ConnectionHandler connectionHandler) {
        super();
        Presentation presentation = getTemplatePresentation();
        presentation.setText(connectionHandler == null ? "No Connection" : connectionHandler.getQualifiedName(), false);
        presentation.setIcon(connectionHandler == null ? Icons.SPACE : connectionHandler.getIcon());
        this.connectionHandler = connectionHandler;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        Editor editor = getEditor(e);
        if (editor != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.setConnectionHandler(editor, connectionHandler);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        boolean enabled = true;
        VirtualFile virtualFile = getVirtualFile(e);
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            enabled = false;
        } else {
            if (virtualFile != null && virtualFile.getFileType() instanceof DBLanguageFileType) {
                if (connectionHandler == null) {
                    enabled = true;
                }
            } else {
                enabled = false;
            }
        }
        presentation.setEnabled(enabled);

    }
}
