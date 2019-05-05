package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

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

        ConnectionAction.invoke(loaded ? "reloading the " + listName : "loading the " + listName, true, objectList,
                (action) -> Progress.prompt(objectList.getProject(), "Reloading " + objectList.getObjectType().getListName(), true,
                        (progress) -> {
                            objectList.getConnectionHandler().getMetaDataCache().reset();
                            objectList.reload();
                        }));
    }
}