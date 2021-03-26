package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.Context;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SelectSessionIntentionAction extends GenericIntentionAction implements LowPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Set current session";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.FILE_SESSION_MAPPING;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            VirtualFile virtualFile = psiFile.getVirtualFile();
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            if (connectionMappingManager.isSessionSelectable(virtualFile)) {
                DBLanguagePsiFile file = (DBLanguagePsiFile) psiFile;
                ConnectionHandler connectionHandler = file.getConnectionHandler();
                return
                    connectionHandler != null &&
                    !connectionHandler.isVirtual() &&
                    connectionHandler.getSettings().getDetailSettings().isEnableSessionManagement();
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguageFile = (DBLanguagePsiFile) psiFile;
            DataContext dataContext = Context.getDataContext(editor);
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.promptSessionSelector(dbLanguageFile, dataContext, null);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return 1;
    }
}
