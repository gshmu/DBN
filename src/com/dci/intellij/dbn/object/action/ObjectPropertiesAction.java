package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ObjectPropertiesAction<T extends DBObject> extends AnObjectAction<T> {
    public ObjectPropertiesAction(T object) {
        super("Properties", null, object);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DBObject object) {

        MessageUtil.showInfoDialog(project, "Not implemented!", "This feature is not implemented yet.");
    }
}
