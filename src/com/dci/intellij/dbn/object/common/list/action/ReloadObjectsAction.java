package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ReloadObjectsAction extends DumbAwareProjectAction {

    private DBObjectList objectList;

    ReloadObjectsAction(DBObjectList objectList) {
        super(objectList.isLoaded() ? "Reload" : "Load", null, Icons.ACTION_REFRESH);
        this.objectList = objectList;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        String listName = objectList.getName();

        ConnectionAction.invoke(
                objectList.isLoaded() ? "reloading the " + listName : "loading the " + listName, true, objectList,
                (action) -> Progress.prompt(project, "Reloading " + objectList.getContentDescription(), true,
                        (progress) -> {
                            objectList.getConnection().getMetaDataCache().reset();
                            objectList.reload();
                        }));
    }
}
