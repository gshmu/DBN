package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class SelectDatabaseSessionAction extends DumbAwareAction {
    private DatabaseSession session;
    public SelectDatabaseSessionAction(DatabaseSession session) {
        super(session.getName(), null, session.getIcon());
        this.session = session;
    }


    @NotNull
    public DatabaseSession getSession() {
        return session;
    }


    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.setDatabaseSession(editor, session);
        }
    }

    public void update(AnActionEvent e) {
        super.update(e);
        boolean enabled = false;
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                enabled = false;//objectFile.getObject().getSchema() == schema;
            } else {
                PsiFile currentFile = PsiUtil.getPsiFile(project, virtualFile);
                enabled = currentFile instanceof DBLanguagePsiFile;
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
