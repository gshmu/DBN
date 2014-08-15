package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

public class SaveChangesAction extends AbstractSourceCodeEditorAction {
    public SaveChangesAction() {
        super("", null, Icons.CODE_EDITOR_SAVE);
    }

    public void actionPerformed(final AnActionEvent e) {
        final Project project = ActionUtil.getProject(e);
        final Editor editor = getEditor(e);
        final DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);

        new WriteActionRunner() {
            public void run() {
                FileDocumentManager.getInstance().saveAllDocuments();
                SourceCodeManager.getInstance(project).updateSourceToDatabase(editor, virtualFile);
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
                    virtualFile.getContentType() == DBContentType.CODE_SPEC ? "Save spec" :
                            virtualFile.getContentType() == DBContentType.CODE_BODY ? "Save body" : "Save";

            DBSchemaObject object = virtualFile.getObject();
            presentation.setEnabled(object != null && !object.getStatus().is(DBObjectStatus.SAVING) && virtualFile.isModified());
            presentation.setText(text);
        }
    }
}
