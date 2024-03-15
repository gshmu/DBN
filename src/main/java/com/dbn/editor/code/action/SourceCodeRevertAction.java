package com.dbn.editor.code.action;

import com.dbn.common.dispose.Checks;
import com.dbn.common.environment.EnvironmentManager;
import com.dbn.common.icon.Icons;
import com.dbn.common.option.ConfirmationOptionHandler;
import com.dbn.editor.code.SourceCodeEditor;
import com.dbn.editor.code.SourceCodeManager;
import com.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dbn.editor.code.options.CodeEditorSettings;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.vfs.file.status.DBFileStatus.LOADING;

public class SourceCodeRevertAction extends AbstractCodeEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull SourceCodeEditor fileEditor, @NotNull DBSourceCodeVirtualFile sourceCodeFile) {
        CodeEditorConfirmationSettings confirmationSettings = CodeEditorSettings.getInstance(project).getConfirmationSettings();
        ConfirmationOptionHandler optionHandler = confirmationSettings.getRevertChanges();
        boolean canContinue = optionHandler.resolve(fileEditor.getObject().getQualifiedNameWithType());

        if (canContinue) {
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
            sourceCodeManager.loadSourceCode(sourceCodeFile, true);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable SourceCodeEditor fileEditor, @Nullable DBSourceCodeVirtualFile sourceCodeFile) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Revert Changes");
        presentation.setIcon(Icons.CODE_EDITOR_RESET);

        if (Checks.isValid(sourceCodeFile)) {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean readonly = environmentManager.isReadonly(sourceCodeFile);
            presentation.setVisible(!readonly);
            presentation.setEnabled(sourceCodeFile.isNot(LOADING) && sourceCodeFile.isModified());
        } else {
            presentation.setEnabled(false);
        }
    }
}
