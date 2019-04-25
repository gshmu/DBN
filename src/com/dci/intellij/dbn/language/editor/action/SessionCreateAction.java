package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SessionCreateAction extends DumbAwareProjectAction {
    private ConnectionHandlerRef connectionHandlerRef;

    SessionCreateAction(ConnectionHandler connectionHandler) {
        super("New Session...");
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookup.getEditor(e);
        if (editor != null) {
            final DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
            ConnectionHandler connectionHandler = connectionHandlerRef.ensure();
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
