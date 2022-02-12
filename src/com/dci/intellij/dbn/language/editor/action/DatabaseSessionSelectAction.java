package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DatabaseSessionSelectAction extends DumbAwareProjectAction {
    private DatabaseSession session;
    DatabaseSessionSelectAction(DatabaseSession session) {
        super(session.getName(), null, session.getIcon());
        this.session = session;
    }


    @NotNull
    public DatabaseSession getSession() {
        return session;
    }


    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookup.getEditor(e);
        if (editor != null) {
            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
            contextManager.setDatabaseSession(editor, session);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        boolean enabled = false;
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        if (virtualFile != null) {
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                enabled = false;
            } else {
                enabled = true;
/*
                // TODO allow selecting "hot" session?
                PsiFile currentFile = PsiUtil.getPsiFile(project, virtualFile);
                if (currentFile instanceof DBLanguagePsiFile) {
                    FileConnectionMappingManager connectionMappingManager = getComponent(e, FileConnectionMappingManager.class);
                    ConnectionHandler connectionHandler = connectionMappingManager.getCache(virtualFile);
                    if (connectionHandler != null) {
                        DBNConnection connection = connectionHandler.getConnectionPool().getSessionConnection(session.getId());
                        enabled = connection == null || !connection.hasDataChanges();
                    }
                }
*/

            }
        }

        Presentation presentation = e.getPresentation();
        if (session.isMain()) {
            presentation.setDescription("Execute statements using main connection");
        } else if (session.isPool()) {
            presentation.setDescription("Execute statements in pool connections (async)");
        }


        presentation.setEnabled(enabled);
    }
}
