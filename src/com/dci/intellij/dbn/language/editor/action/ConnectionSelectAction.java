package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ConnectionSelectAction extends DumbAwareProjectAction {
    private final ConnectionHandler connectionHandler;

    ConnectionSelectAction(ConnectionHandler connectionHandler) {
        super();
        Presentation presentation = getTemplatePresentation();
        presentation.setText(connectionHandler == null ? "No Connection" : connectionHandler.getQualifiedName(), false);
        presentation.setIcon(connectionHandler == null ? Icons.SPACE : connectionHandler.getIcon());
        this.connectionHandler = connectionHandler;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookup.getEditor(e);
        if (editor != null) {
            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
            contextManager.setConnection(editor, connectionHandler);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        boolean enabled = true;
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
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
