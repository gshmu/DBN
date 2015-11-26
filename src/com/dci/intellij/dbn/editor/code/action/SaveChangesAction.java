package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.option.ConfirmationOptionHandler;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
            ConfirmationOptionHandler optionHandler = confirmationSettings.getSaveChangesOptionHandler();
            boolean canContinue = optionHandler.resolve(fileEditor.getObject().getQualifiedNameWithType());
            if (canContinue) {
                new WriteActionRunner() {
                    public void run() {
                        FileDocumentManager.getInstance().saveAllDocuments();
                        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                        sourceCodeManager.updateSourceToDatabase(fileEditor, null);
                    }
                }.start();
            }

        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        if (virtualFile == null) {
            presentation.setEnabled(false);
        } else {
            String text =
                    virtualFile.getContentType() == DBContentType.CODE_SPEC ? "Save spec" :
                    virtualFile.getContentType() == DBContentType.CODE_BODY ? "Save body" : "Save";

            DBSchemaObject object = virtualFile.getObject();
            presentation.setEnabled(!object.getStatus().is(DBObjectStatus.SAVING) && virtualFile.isModified());
            presentation.setText(text);
        }
    }
}
