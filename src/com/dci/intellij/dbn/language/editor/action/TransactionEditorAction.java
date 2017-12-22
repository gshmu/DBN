package com.dci.intellij.dbn.language.editor.action;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionPool;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
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
        boolean visible = false;
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        if (connectionHandler != null) {
            DatabaseSession databaseSession = getDatabaseSession(e);
            if (databaseSession != null && !databaseSession.isPool()) {
                ConnectionPool connectionPool = connectionHandler.getConnectionPool();
                DBNConnection sessionConnection = connectionPool.getSessionConnection(databaseSession.getId());
                if (sessionConnection != null && !sessionConnection.getAutoCommit() && sessionConnection.hasDataChanges()) {
                    enabled = true;
                    visible = true;
                    if (virtualFile instanceof DBEditableObjectVirtualFile) {
                        DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
                        DBSchemaObject object = databaseFile.getObject();
                        if (object instanceof DBTable) {
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
        Project project = getEventProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            return connectionMappingManager.getConnectionHandler(virtualFile);
        }
        return null;
    }

    @Nullable
    protected DatabaseSession getDatabaseSession(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
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
