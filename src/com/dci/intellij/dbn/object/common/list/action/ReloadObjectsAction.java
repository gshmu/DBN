package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

public class ReloadObjectsAction extends AnAction {

    private DBObjectList objectList;

    public ReloadObjectsAction(DBObjectList objectList) {
        super("Reload", null, Icons.ACTION_REFRESH);
        this.objectList = objectList;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        new BackgroundTask(objectList.getProject(), "Reloading " + objectList.getContentDescription() + ".", false) {
            @Override
            public void execute(@NotNull final ProgressIndicator progressIndicator) throws InterruptedException {
                initProgressIndicator(progressIndicator, true);
                objectList.reload();
            }
        }.start();

    }
}
