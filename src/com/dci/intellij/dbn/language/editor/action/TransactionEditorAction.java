package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.Icon;

public abstract class TransactionEditorAction extends DumbAwareAction {
    protected TransactionEditorAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    public void update(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        boolean enabled = false;
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getActiveConnection(virtualFile);
            enabled = activeConnection != null && activeConnection.hasUncommittedChanges();
        }
        e.getPresentation().setEnabled(enabled);
    }

    protected void showErrorDialog(final String message) {
        new SimpleLaterInvocator() {
            public void execute() {
                MessageUtil.showErrorDialog(message);
            }
        }.start();
    }

    protected ConnectionHandler getConnectionHandler(Project project, VirtualFile virtualFile) {
        if (project != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            return connectionMappingManager.getActiveConnection(virtualFile);
        }
        return null;
    }
}
