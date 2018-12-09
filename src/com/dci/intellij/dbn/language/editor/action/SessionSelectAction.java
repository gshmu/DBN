package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.ActionUtil.getEditor;
import static com.dci.intellij.dbn.common.util.ActionUtil.getProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class SessionSelectAction extends DumbAwareAction {
    private DatabaseSession session;
    public SessionSelectAction(DatabaseSession session) {
        super(session.getName(), null, session.getIcon());
        this.session = session;
    }


    @NotNull
    public DatabaseSession getSession() {
        return session;
    }


    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getProject(e);
        Editor editor = getEditor(e);
        if (project != null && editor != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.setDatabaseSession(editor, session);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        boolean enabled = false;
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (project != null &&  virtualFile != null) {
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                enabled = false;
            } else {
                enabled = true;
/*
                // TODO allow selecting "hot" session?
                PsiFile currentFile = PsiUtil.getPsiFile(project, virtualFile);
                if (currentFile instanceof DBLanguagePsiFile) {
                    FileConnectionMappingManager connectionMappingManager = getComponent(e, FileConnectionMappingManager.class);
                    ConnectionHandler connectionHandler = connectionMappingManager.getConnectionHandler(virtualFile);
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
