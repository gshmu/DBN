package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.Progress;
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
        String connectionName = connectionHandler.getName();
        Progress.prompt(
                project,
                "Loading data dictionary (" + connectionName + ")", true,
                (progress) -> {
                    DBObjectListContainer objectListContainer = connectionHandler.getObjectBundle().getObjectListContainer();
                    objectListContainer.visitLists(DBObjectRecursiveLoaderVisitor.INSTANCE, false);
                });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(DatabaseNavigator.developerModeEnabled);
    }
}
