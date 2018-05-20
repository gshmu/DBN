package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ReloadObjectsAction extends AnAction {

    private DBObjectList objectList;

    public ReloadObjectsAction(DBObjectList objectList) {
        super(objectList.isLoaded() ? "Reload" : "Load", null, Icons.ACTION_REFRESH);
        this.objectList = objectList;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        TaskInstructions taskInstructions = new TaskInstructions("Reloading " + objectList.getContentDescription() + ".");
        String listName = objectList.getName();
        final boolean loaded = objectList.isLoaded();
        String actionDescription = loaded ? "reloading the " + listName : "loading the " + listName;
        new ConnectionAction(actionDescription, objectList, taskInstructions) {
            @Override
            protected void execute() {
                if (loaded)
                    objectList.reload(); else
                    objectList.load(true);
            }
        }.start();
    }
}
