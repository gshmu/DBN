package com.dci.intellij.dbn.language.editor.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import static com.dci.intellij.dbn.common.util.ActionUtil.*;

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


    public void actionPerformed(AnActionEvent e) {
        Project project = getProject(e);
        Editor editor = getEditor(e);
        if (project != null && editor != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.setDatabaseSession(editor, session);
        }
    }

    public void update(AnActionEvent e) {
        super.update(e);
        boolean enabled = false;
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (project != null &&  virtualFile != null) {
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                enabled = false;//objectFile.getObject().getSchema() == schema;
            } else {
                PsiFile currentFile = PsiUtil.getPsiFile(project, virtualFile);
                if (currentFile instanceof DBLanguagePsiFile) {
                    FileConnectionMappingManager connectionMappingManager = getComponent(e, FileConnectionMappingManager.class);
                    DBNConnection connection = connectionMappingManager.getConnection(virtualFile);
                    enabled = connection == null || !connection.hasDataChanges();
                }

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
