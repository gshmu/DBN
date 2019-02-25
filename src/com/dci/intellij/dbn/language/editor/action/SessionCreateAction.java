package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.ActionUtil.ensureProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getEditor;

public class SessionCreateAction extends DumbAwareAction {
    private ConnectionHandlerRef connectionHandlerRef;

    SessionCreateAction(ConnectionHandler connectionHandler) {
        super("New session...");
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        Editor editor = getEditor(e);
        if (editor != null) {
            final DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
            ConnectionHandler connectionHandler = connectionHandlerRef.getnn();
            sessionManager.showCreateSessionDialog(
                    connectionHandler,
                    (session) -> {
                        if (session != null) {
                            FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
                            mappingManager.setDatabaseSession(editor, session);
                        }
                    });
        }
    }
}
