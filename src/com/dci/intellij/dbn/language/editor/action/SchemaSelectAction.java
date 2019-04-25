package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class SchemaSelectAction extends AnObjectAction<DBSchema> {
    SchemaSelectAction(DBSchema schema) {
        super(schema);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DBSchema object) {
        Editor editor = Lookup.getEditor(e);
        if (editor != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.setDatabaseSchema(editor, SchemaId.from(object));
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        super.update(e, project);
        boolean enabled = false;
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            enabled = false;//objectFile.getObject().getSchema() == schema;
        } else if (virtualFile != null){
            PsiFile currentFile = PsiUtil.getPsiFile(project, virtualFile);
            enabled = currentFile instanceof DBLanguagePsiFile;
        }

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
    }
}
