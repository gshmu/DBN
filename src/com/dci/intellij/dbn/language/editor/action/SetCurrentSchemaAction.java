package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class SetCurrentSchemaAction extends DumbAwareAction {
    private DBSchema schema;

    public SetCurrentSchemaAction(DBSchema schema) {
        super(schema.getName(), null, schema.getIcon());
        this.schema = schema;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            FileConnectionMappingManager.getInstance(project).setCurrentSchemaForSelectedEditor(editor, schema);
        }
    }

    public void update(AnActionEvent e) {
        boolean enabled = true;
        Project project = ActionUtil.getProject(e);

        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile instanceof DatabaseEditableObjectFile) {
            enabled = false;//objectFile.getObject().getSchema() == schema;
        } else {
            PsiFile currentFile = PsiUtil.getPsiFile(project, virtualFile);
            enabled = currentFile instanceof DBLanguageFile;
        }

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        presentation.setText(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()));
        presentation.setIcon(schema.getIcon());
        presentation.setDescription(schema.getDescription());

    }
}
