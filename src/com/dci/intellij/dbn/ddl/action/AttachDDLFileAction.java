package com.dci.intellij.dbn.ddl.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AttachDDLFileAction extends DumbAwareProjectAction {
    private DBObjectRef<DBSchemaObject> objectRef;

    public AttachDDLFileAction(@NotNull DBSchemaObject object) {
        super("Attach files");
        this.objectRef = DBObjectRef.from(object);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        fileAttachmentManager.attachDDLFiles(objectRef);
    }
}
