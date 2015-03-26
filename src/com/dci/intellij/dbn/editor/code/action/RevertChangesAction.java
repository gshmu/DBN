package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.option.ConfirmationOptionHandler;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;

public class RevertChangesAction extends AbstractSourceCodeEditorAction {
    public RevertChangesAction() {
        super("Revert changes", null, Icons.CODE_EDITOR_RESET);
    }

    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project project = ActionUtil.getProject(e);
        final DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        final FileEditor fileEditor = getFileEditor(e);
        if (project != null && virtualFile != null && fileEditor != null) {
            CodeEditorConfirmationSettings confirmationSettings = CodeEditorSettings.getInstance(project).getConfirmationSettings();
            ConfirmationOptionHandler optionHandler = confirmationSettings.getRevertChangesOptionHandler();
            boolean canContinue = optionHandler.resolve(virtualFile.getMainDatabaseFile().getObject().getQualifiedNameWithType());

            if (canContinue) {
                TaskInstructions taskInstructions = new TaskInstructions("Reverting local changes", false, false);
                new ConnectionAction("reverting the changes", virtualFile, taskInstructions) {
                    @Override
                    protected void execute() {
                        final Editor editor = getEditor(e);
                        final DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);

                        if (editor != null && sourceCodeFile != null) {
                            boolean reloaded = sourceCodeFile.reloadFromDatabase();

                            if (reloaded) {
                                new WriteActionRunner() {
                                    public void run() {
                                        editor.getDocument().setText(sourceCodeFile.getContent());
                                        sourceCodeFile.setModified(false);
                                    }
                                }.start();
                            }
                        }
                    }
                }.start();
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(virtualFile!= null && virtualFile.isModified());
        presentation.setText("Revert Changes");
    }
}
