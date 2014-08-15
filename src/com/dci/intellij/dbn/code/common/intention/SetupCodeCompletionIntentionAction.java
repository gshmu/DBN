package com.dci.intellij.dbn.code.common.intention;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class SetupCodeCompletionIntentionAction extends GenericIntentionAction {
    @NotNull
    public String getText() {
        return "Setup code completion";
    }

    @NotNull
    public String getFamilyName() {
        return "Setup intentions";
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return psiFile instanceof DBLanguagePsiFile && psiFile.getVirtualFile().getParent() != null;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        GlobalProjectSettingsDialog globalSettingsDialog = new GlobalProjectSettingsDialog(project);
        CodeCompletionSettings settings = CodeCompletionSettings.getInstance(project);
        globalSettingsDialog.focusSettings(settings);
        globalSettingsDialog.show();
    }

    public boolean startInWriteAction() {
        return false;
    }
}