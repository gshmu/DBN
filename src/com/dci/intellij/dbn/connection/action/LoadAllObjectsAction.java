package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectRecursiveLoaderVisitor;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class LoadAllObjectsAction extends AbstractConnectionAction {
    LoadAllObjectsAction(ConnectionHandler connectionHandler) {
        super("Load All Objects", null, Icons.DATA_EDITOR_RELOAD_DATA, connectionHandler);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        ConnectionHandler connectionHandler = getConnectionHandler();
        String taskTitle = "Loading data dictionary (" + connectionHandler.getName() + ")";
        BackgroundTask.invoke(project, taskTitle, true, false, (task, progress) -> {
            DBObjectListContainer objectListContainer = connectionHandler.getObjectBundle().getObjectListContainer();
            objectListContainer.visitLists(DBObjectRecursiveLoaderVisitor.INSTANCE, false);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DatabaseNavigator databaseNavigator = DatabaseNavigator.getInstance();
        e.getPresentation().setVisible(databaseNavigator.isDeveloperModeEnabled());
        super.update(e);
    }
}
