package com.dci.intellij.dbn.object.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ObjectListFilterAction extends AnAction {

    private DBObjectList objectList;

    public ObjectListFilterAction(DBObjectList objectList) {
        super("Filter... ");
        this.objectList = objectList;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        TaskInstructions taskInstructions = new TaskInstructions("Reloading " + objectList.getObjectType().getListName(), false, false);
        new ConnectionAction("reloading the objects", objectList, taskInstructions){
            @Override
            protected void execute() {
                objectList.reload();
            }
        }.start();
    }
}