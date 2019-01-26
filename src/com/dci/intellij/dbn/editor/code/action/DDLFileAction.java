package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.ddl.action.AttachDDLFileAction;
import com.dci.intellij.dbn.ddl.action.CreateDDLFileAction;
import com.dci.intellij.dbn.ddl.action.DetachDDLFileAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class DDLFileAction extends GroupPopupAction {
    public DDLFileAction() {
        super("DDL File", "DDL File", Icons.CODE_EDITOR_DDL_FILE);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.CODE_EDITOR_DDL_FILE);
        presentation.setText("DDL Files");
        presentation.setEnabled(sourceCodeFile != null);
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        if (sourceCodeFile != null) {
            DBSchemaObject object = sourceCodeFile.getObject();
            return new AnAction[]{
                    new CreateDDLFileAction(object),
                    new AttachDDLFileAction(object),
                    new DetachDDLFileAction(object)
            };
        }
        return new AnAction[0];
    }

    protected static DBSourceCodeVirtualFile getSourcecodeFile(AnActionEvent e) {
        VirtualFile virtualFile = getVirtualFile(e);
        return virtualFile instanceof DBSourceCodeVirtualFile ? (DBSourceCodeVirtualFile) virtualFile : null;
    }

}
