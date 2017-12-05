package com.dci.intellij.dbn.object.common.list.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ReloadObjectsAction extends AnAction {

    private DBObjectList objectList;

    public ReloadObjectsAction(DBObjectList objectList) {
        super(objectList.isLoaded() ? "Reload" : "Load", null, Icons.ACTION_REFRESH);
        this.objectList = objectList;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        TaskInstructions taskInstructions = new TaskInstructions("Reloading " + objectList.getContentDescription() + ".", false, false);
        String listName = objectList.getName();
        String actionDescription = objectList.isLoaded() ? "reloading the " + listName : "loading the " + listName;
        new ConnectionAction(actionDescription, objectList, taskInstructions) {
            @Override
            protected void execute() {
                objectList.reload();
            }
        }.start();
    }
}
