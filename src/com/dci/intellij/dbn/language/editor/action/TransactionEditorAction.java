package com.dci.intellij.dbn.language.editor.action;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class TransactionEditorAction extends DumbAwareAction {
    protected TransactionEditorAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    public void update(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        boolean enabled = false;
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getActiveConnection(virtualFile);
            enabled = activeConnection != null && activeConnection.hasUncommittedChanges();
        }
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);

        ConnectionHandler connectionHandler = getConnectionHandler(project, virtualFile);
        if (project == null || connectionHandler == null) {
            presentation.setVisible(false);
        } else {
            boolean isEnvironmentReadonlyData = false;
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
                DBSchemaObject object = databaseFile.getObject();
                if (object instanceof DBTable) {
                    isEnvironmentReadonlyData = EnvironmentManager.getInstance(project).isReadonly(object, DBContentType.DATA);
                }
            }
            presentation.setVisible(!isEnvironmentReadonlyData && !connectionHandler.isAutoCommit());
        }
    }

    protected static void showErrorDialog(final Project project, final String message) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                MessageUtil.showErrorDialog(project, message);
            }
        }.start();
    }

    protected static ConnectionHandler getConnectionHandler(Project project, VirtualFile virtualFile) {
        if (project != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            return connectionMappingManager.getActiveConnection(virtualFile);
        }
        return null;
    }
}
