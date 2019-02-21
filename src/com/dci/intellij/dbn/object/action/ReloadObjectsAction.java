package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class ReloadObjectsAction extends DumbAwareAction {

    private DBObjectList objectList;

    ReloadObjectsAction(DBObjectList objectList) {
        super((objectList.isLoaded() ? "Reload " : "Load ") + objectList.getName());
        this.objectList = objectList;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String listName = objectList.getName();
        boolean loaded = objectList.isLoaded();

        ConnectionAction.invoke(
                instructions("Reloading " + objectList.getObjectType().getListName()),
                loaded ? "reloading the " + listName : "loading the " + listName,
                objectList,
                action -> {
                    objectList.reload();
                });
    }
}