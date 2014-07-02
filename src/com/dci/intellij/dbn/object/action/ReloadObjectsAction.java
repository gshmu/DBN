package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ReloadObjectsAction extends AnAction {

    private DBObjectList objectList;

    public ReloadObjectsAction(DBObjectList objectList) {
        super("Reload " + objectList.getObjectType().getListName());
        this.objectList = objectList;
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = ActionUtil.getProject(event);
        if (project != null) {
            new BackgroundTask(project, "Reloading " + objectList.getObjectType().getListName(), false) {
                @Override
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    initProgressIndicator(progressIndicator, false);
                    objectList.reload();
                }
            }.start();
        }
    }
}