package com.dci.intellij.dbn.language.editor.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class SetCurrentSchemaAction extends DumbAwareAction {
    private DBObjectRef<DBSchema> schemaRef;

    public SetCurrentSchemaAction(DBSchema schema) {
        super(schema.getName(), null, schema.getIcon());
        this.schemaRef = DBObjectRef.from(schema);
    }


    @NotNull
    public DBSchema getSchema() {
        return DBObjectRef.getnn(schemaRef);
    }


    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            DBSchema schema = getSchema();
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.setCurrentSchemaForEditor(editor, schema);
        }
    }

    public void update(AnActionEvent e) {
        boolean enabled= false;
        Project project = ActionUtil.getProject(e);
        DBSchema schema = getSchema();
        if (project != null) {
            VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                enabled = false;//objectFile.getObject().getSchema() == schema;
            } else {
                PsiFile currentFile = PsiUtil.getPsiFile(project, virtualFile);
                enabled = currentFile instanceof DBLanguagePsiFile;
            }
        }


        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        presentation.setText(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()));
        presentation.setIcon(schema.getIcon());
        presentation.setDescription(schema.getDescription());

    }
}
