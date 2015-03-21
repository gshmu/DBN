package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;

public class ReloadSourceCodeAction extends AbstractSourceCodeEditorAction {
    public ReloadSourceCodeAction() {
        super("", null, Icons.CODE_EDITOR_RELOAD);
    }

    public void actionPerformed(@NotNull final AnActionEvent e) {
        DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);
        TaskInstructions taskInstructions = new TaskInstructions("Loading database source code", false, true);
        new ConnectionAction("reloading the source code", sourcecodeFile, taskInstructions){
            @Override
            protected void execute() {
                final Editor editor = getEditor(e);
                final DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);

                if (editor != null && sourcecodeFile != null) {
                    boolean reloaded = sourcecodeFile.reloadFromDatabase();
                    if (reloaded) {
                        new WriteActionRunner() {
                            public void run() {
                                editor.getDocument().setText(sourcecodeFile.getContent());
                                sourcecodeFile.setModified(false);
                            }
                        }.start();
                    }
                }
            }
        }.start();
    }

    public void update(AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        if (virtualFile == null) {
            presentation.setEnabled(false);
        } else {
            String text =
                virtualFile.getContentType() == DBContentType.CODE_SPEC ? "Reload spec" :
                virtualFile.getContentType() == DBContentType.CODE_BODY ? "Reload body" : "Reload";

            presentation.setText(text);
            presentation.setEnabled(!virtualFile.isModified());
        }
    }
}