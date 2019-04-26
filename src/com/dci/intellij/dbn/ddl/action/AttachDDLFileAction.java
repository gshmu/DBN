package com.dci.intellij.dbn.ddl.action;

import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AttachDDLFileAction extends AnObjectAction<DBSchemaObject> {
    public AttachDDLFileAction(@NotNull DBSchemaObject object) {
        super("Attach files", null, object);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DBSchemaObject target) {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        fileAttachmentManager.attachDDLFiles(target.getRef());
    }

}
