package com.dci.intellij.dbn.ddl.action;

import com.dci.intellij.dbn.common.action.ProjectAction;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DDLFileDetachAction extends ProjectAction {
    private DBObjectRef<DBSchemaObject> objectRef;
    public DDLFileDetachAction(DBSchemaObject object) {
        super("Detach files");
        this.objectRef = DBObjectRef.of(object);
    }


    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        fileAttachmentManager.detachDDLFiles(objectRef);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        boolean hasAttachedDDLFiles = fileAttachmentManager.hasAttachedDDLFiles(objectRef);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(hasAttachedDDLFiles);
    }
}