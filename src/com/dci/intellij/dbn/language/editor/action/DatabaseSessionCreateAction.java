package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookups;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatabaseSessionCreateAction extends DumbAwareProjectAction {
    private final ConnectionRef connection;

    DatabaseSessionCreateAction(ConnectionHandler connection) {
        super("New Session...");
        this.connection = connection.ref();
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookups.getEditor(e);
        if (editor != null) {
            DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
            ConnectionHandler connection = this.connection.ensure();
            sessionManager.showCreateSessionDialog(
                    connection,
                    (session) -> {
                        if (session != null) {
                            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
                            contextManager.setDatabaseSession(editor, session);
                        }
                    });
        }
    }
}
