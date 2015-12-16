package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.option.ConfirmationOptionHandler;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class SaveChangesAction extends AbstractSourceCodeEditorAction {
    public SaveChangesAction() {
        super("", null, Icons.CODE_EDITOR_SAVE_TO_DATABASE);
    }

    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project project = ActionUtil.getProject(e);
        final SourceCodeEditor fileEditor = getFileEditor(e);
        if (project != null && fileEditor != null) {
            CodeEditorConfirmationSettings confirmationSettings = CodeEditorSettings.getInstance(project).getConfirmationSettings();
            ConfirmationOptionHandler optionHandler = confirmationSettings.getSaveChanges();
            boolean canContinue = optionHandler.resolve(fileEditor.getObject().getQualifiedNameWithType());
            if (canContinue) {
                DBSourceCodeVirtualFile sourceCodeFile = fileEditor.getVirtualFile();
                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                sourceCodeManager.saveSourceCode(sourceCodeFile, fileEditor, null);
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        Project project = e.getProject();
        if (project == null || sourceCodeFile == null) {
            presentation.setEnabled(false);
        } else {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean readonly = environmentManager.isReadonly(sourceCodeFile);
            presentation.setVisible(!readonly);
            DBContentType contentType = sourceCodeFile.getContentType();
            String text =
                    contentType == DBContentType.CODE_SPEC ? "Save spec" :
                    contentType == DBContentType.CODE_BODY ? "Save body" : "Save";

            presentation.setEnabled(sourceCodeFile.isModified() && !sourceCodeFile.isSaving());
            presentation.setText(text);
        }
    }
}
