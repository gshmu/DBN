package com.dci.intellij.dbn.connection;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;

public abstract class ConnectionAction extends SimpleTask {
    public static final String[] OPTIONS_CONNECT_CANCEL = new String[]{"Connect", "Cancel"};

    private String name;
    private ConnectionProvider connectionProvider;
    private TaskInstructions taskInstructions;

    public ConnectionAction(String name, ConnectionProvider connectionProvider) {
        this.name = name;
        this.connectionProvider = connectionProvider;
    }

    public ConnectionAction(String name, ConnectionProvider connectionProvider, TaskInstructions taskInstructions) {
        this.name = name;
        this.connectionProvider = connectionProvider;
        this.taskInstructions = taskInstructions;
    }

    @NotNull
    protected Project getProject() {
        return getConnectionHandler().getProject();
    }

    protected boolean isCanceled() {
        ProgressIndicator progressIndicator = getProgressIndicator();
        return progressIndicator != null && progressIndicator.isCanceled();
    }

    protected ProgressIndicator getProgressIndicator() {
        return ProgressManager.getInstance().getProgressIndicator();
    }

    public final void start() {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            run();
        } else {
            application.invokeLater(this/*, ModalityState.NON_MODAL*/);
        }
    }

    public final void run() {
        try {
            if (canExecute()) {
                final ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler.isVirtual() || connectionHandler.canConnect()) {
                    doExecute();
                } else {
                    if (connectionHandler.isAuthenticationProvided()) {
                        MessageUtil.showInfoDialog(
                                connectionHandler.getProject(),
                                "Not Connected to Database",
                                "You are not connected to database \"" + connectionHandler.getName() + "\". \n" +
                                        "If you want to continue with " + name + ", you need to connect.",
                                OPTIONS_CONNECT_CANCEL, 0,
                                new SimpleTask() {
                                    @Override
                                    protected void execute() {
                                        if (getOption() == 0) {
                                            connectionHandler.setAllowConnection(true);
                                            doExecute();
                                        } else {
                                            ConnectionAction.this.cancel();
                                            cancel();
                                        }
                                    }
                                });
                    } else {
                        Authentication authentication = connectionHandler.getSettings().getDatabaseSettings().getAuthentication();
                        authentication = ConnectionManager.openUserPasswordDialog(getProject(), connectionHandler, authentication.clone());
                        if (authentication != null) {
                            doExecute();
                        } else {
                            cancel();
                        }
                    }
                }
            } else {
                cancel();
            }
        } catch (ProcessCanceledException e) {
            // do nothing
        }

    }

    private void doExecute() {
        if (taskInstructions == null) {
            execute();
        } else {
            ConnectionHandler connectionHandler = getConnectionHandler();
            new BackgroundTask(connectionHandler.getProject(), taskInstructions) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    ConnectionAction.this.execute();
                }
            }.start();
        }
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        ConnectionHandler connectionHandler = connectionProvider.getConnectionHandler();
        return FailsafeUtil.get(connectionHandler);
    }

    protected abstract void execute();
}
