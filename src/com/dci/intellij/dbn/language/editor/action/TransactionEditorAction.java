package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.util.ActionUtil.*;

public abstract class TransactionEditorAction extends DumbAwareAction {
    TransactionEditorAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    public void update(@NotNull AnActionEvent e) {
        VirtualFile virtualFile = getVirtualFile(e);
        boolean enabled = false;
        boolean visible = false;
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        if (connectionHandler != null && !connectionHandler.isVirtual()) {

            DatabaseSession session = getDatabaseSession(e);
            if (session != null && !session.isPool()) {
                DBNConnection connection = getConnection(e);
                if (connection != null && !connection.isPoolConnection() && connection.hasDataChanges()) {
                    enabled = true;
                }

                if (!connectionHandler.isAutoCommit()) {
                    visible = true;
                    if (virtualFile instanceof DBEditableObjectVirtualFile) {
                        DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
                        DBSchemaObject object = databaseFile.getObject();
                        if (object instanceof DBTable) {
                            Project project = ensureProject(e);
                            visible = !EnvironmentManager.getInstance(project).isReadonly(object, DBContentType.DATA);
                        }
                    }
                }

            }
        }


        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        presentation.setVisible(visible);
    }

    @Nullable
    protected ConnectionHandler getConnectionHandler(@NotNull AnActionEvent e) {
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            return connectionMappingManager.getConnectionHandler(virtualFile);
        }
        return null;
    }

    @Nullable
    protected DatabaseSession getDatabaseSession(@NotNull AnActionEvent e) {
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            return connectionMappingManager.getDatabaseSession(virtualFile);
        }
        return null;
    }

    @Nullable
    protected DBNConnection getConnection(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        DatabaseSession databaseSession = getDatabaseSession(e);
        if (connectionHandler != null && databaseSession != null) {
            return connectionHandler.getConnectionPool().getSessionConnection(databaseSession.getId());
        }
        return null;
    }
}
