package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

import static com.dci.intellij.dbn.common.util.ActionUtil.getEditor;
import static com.dci.intellij.dbn.common.util.ActionUtil.getProject;

public class SessionCreateAction extends DumbAwareAction {
    private ConnectionHandlerRef connectionHandlerRef;

    public SessionCreateAction(ConnectionHandler connectionHandler) {
        super("New session...");
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = getProject(e);
        final Editor editor = getEditor(e);
        if (project != null && editor != null) {
            final DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
            ConnectionHandler connectionHandler = connectionHandlerRef.get();
            sessionManager.showCreateSessionDialog(connectionHandler, new SimpleTask<DatabaseSession>() {
                @Override
                protected void execute() {
                    DatabaseSession session = getData();
                    if (session != null) {
                        FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
                        mappingManager.setDatabaseSession(editor, session);
                    }
                }
            });
        }
    }
}
