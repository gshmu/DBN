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
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class RevertChangesAction extends AbstractSourceCodeEditorAction {
    public RevertChangesAction() {
        super("Revert changes", null, Icons.CODE_EDITOR_RESET);
    }

    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project project = ActionUtil.getProject(e);
        SourceCodeEditor fileEditor = getFileEditor(e);
        if (project != null && fileEditor != null) {
            CodeEditorConfirmationSettings confirmationSettings = CodeEditorSettings.getInstance(project).getConfirmationSettings();
            ConfirmationOptionHandler optionHandler = confirmationSettings.getRevertChangesOptionHandler();
            boolean canContinue = optionHandler.resolve(fileEditor.getObject().getQualifiedNameWithType());

            if (canContinue) {
                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                sourceCodeManager.loadSourceFromDatabase(fileEditor.getVirtualFile());
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Revert Changes");
        Project project = e.getProject();
        if (project == null || sourceCodeFile == null) {
            presentation.setEnabled(false);
        } else {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean readonly = environmentManager.isReadonly(sourceCodeFile);
            presentation.setVisible(!readonly);
            DBSchemaObject object = sourceCodeFile.getObject();
            DBContentType contentType = sourceCodeFile.getContentType();
            presentation.setEnabled(!sourceCodeFile.isLoading() && sourceCodeFile.isModified());
        }



    }
}
