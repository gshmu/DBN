package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ObjectPropertiesAction extends DumbAwareAction {
    private DBObject object;
    public ObjectPropertiesAction(DBObject object) {
        super("Properties");
        this.object = object;

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        MessageUtil.showInfoDialog(project, "Not implemented!", "This feature is not implemented yet.");
    }
}
