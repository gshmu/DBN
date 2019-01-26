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

    ReloadObjectsAction(DBObjectList objectList) {
        super(objectList.isLoaded() ? "Reload" : "Load", null, Icons.ACTION_REFRESH);
        this.objectList = objectList;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String listName = objectList.getName();
        final boolean loaded = objectList.isLoaded();
        String actionDescription = loaded ? "reloading the " + listName : "loading the " + listName;
        ConnectionAction.invoke(
                actionDescription,
                objectList,
                TaskInstructions.create("Reloading " + objectList.getContentDescription() + "."),
                action -> {
                    if (loaded)
                        objectList.reload(); else
                        objectList.load(true);
                });
    }
}
