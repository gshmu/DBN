package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ModuleConnectionBundle;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SelectConnectionAction extends DumbAwareAction {
    private final ConnectionHandler connectionHandler;

    public SelectConnectionAction(ConnectionHandler connectionHandler) {
        super(connectionHandler == null ? "No Connection" : NamingUtil.enhanceUnderscoresForDisplay(connectionHandler.getQualifiedName()), null,
                connectionHandler == null ? Icons.SPACE : connectionHandler.getIcon());
        this.connectionHandler = connectionHandler;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            FileConnectionMappingManager.getInstance(project).selectActiveConnectionForEditor(editor, connectionHandler);
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        boolean enabled = true;
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile instanceof DatabaseEditableObjectFile) {
            enabled = false;
        } else {
            if (virtualFile != null && virtualFile.getFileType() instanceof DBLanguageFileType) {
                if (connectionHandler == null) {
                    enabled = true;
                } else {
                    ConnectionBundle connectionBundle = connectionHandler.getConnectionBundle();
                    if (connectionBundle instanceof ModuleConnectionBundle) {
                        Module currentModule = ModuleUtil.findModuleForFile(virtualFile, project);
                        Module connectionModule = ((ModuleConnectionBundle) connectionBundle).getModule();
                        enabled = connectionModule == currentModule;
                    }
                }
            } else {
                enabled = false;
            }
        }
        presentation.setEnabled(enabled);

    }
}
