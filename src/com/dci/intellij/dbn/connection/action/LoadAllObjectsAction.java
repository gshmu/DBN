package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectRecursiveLoaderVisitor;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class LoadAllObjectsAction extends AbstractConnectionAction {
    LoadAllObjectsAction(ConnectionHandler connectionHandler) {
        super("Load All Objects", null, Icons.DATA_EDITOR_RELOAD_DATA, connectionHandler);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        ConnectionHandler connectionHandler = getConnectionHandler();
        String connectionName = connectionHandler.getName();
        BackgroundTask.invoke(project,
                instructions("Loading data dictionary (" + connectionName + ")", TaskInstruction.BACKGROUNDED),
                (data, progress) -> {
                    DBObjectListContainer objectListContainer = connectionHandler.getObjectBundle().getObjectListContainer();
                    objectListContainer.visitLists(DBObjectRecursiveLoaderVisitor.INSTANCE, false);
                });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(DatabaseNavigator.developerModeEnabled);
    }
}
