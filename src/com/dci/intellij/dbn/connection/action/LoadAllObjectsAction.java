package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DatabaseLoadMonitor;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectRecursiveLoaderVisitor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class LoadAllObjectsAction extends AbstractConnectionAction {
    public LoadAllObjectsAction(ConnectionHandler connectionHandler) {
        super("Load All Objects", null, Icons.DATA_EDITOR_RELOAD_DATA, connectionHandler);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        final ConnectionHandler connectionHandler = getConnectionHandler();
        new BackgroundTask(project, "Loading data dictionary (" + connectionHandler.getName() + ")", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                try {
                    DatabaseLoadMonitor.startBackgroundLoad();
                    connectionHandler.getObjectBundle().getObjectListContainer().visitLists(DBObjectRecursiveLoaderVisitor.INSTANCE, false);
                } finally {
                    DatabaseLoadMonitor.endBackgroundLoad();
                }

            }
        }.start();
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setVisible(DatabaseNavigator.getInstance().isDeveloperModeEnabled());
        super.update(e);
    }
}
