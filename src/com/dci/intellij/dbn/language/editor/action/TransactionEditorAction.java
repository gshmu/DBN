package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class TransactionEditorAction extends DumbAwareProjectAction {
    TransactionEditorAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
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
                            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
                            visible = !environmentManager.isReadonly(object, DBContentType.DATA);
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
        Project project = Lookup.getProject(e);
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            return connectionMappingManager.getConnection(virtualFile);
        }
        return null;
    }

    @Nullable
    protected DatabaseSession getDatabaseSession(@NotNull AnActionEvent e) {
        Project project = Lookup.getProject(e);
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
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
