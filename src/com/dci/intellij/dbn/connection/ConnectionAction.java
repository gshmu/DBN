package com.dci.intellij.dbn.connection;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;

public abstract class ConnectionAction extends SimpleTask<Integer> {
    public static final String[] OPTIONS_CONNECT_CANCEL = new String[]{"Connect", "Cancel"};

    private String description;
    private ConnectionProvider connectionProvider;
    private TaskInstructions taskInstructions;

    public ConnectionAction(String description, ConnectionProvider connectionProvider) {
        this.description = description;
        this.connectionProvider = connectionProvider;
    }

    public ConnectionAction(String description, ConnectionProvider connectionProvider, TaskInstructions taskInstructions) {
        this.description = description;
        this.connectionProvider = connectionProvider;
        this.taskInstructions = taskInstructions;
    }

    @NotNull
    protected Project getProject() {
        return getConnectionHandler().getProject();
    }

    protected boolean isCancelled() {
        if (super.isCancelled()) {
            return true;
        } else {
            ProgressIndicator progressIndicator = getProgressIndicator();
            return progressIndicator != null && progressIndicator.isCanceled();
        }
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
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler.isVirtual() || connectionHandler.canConnect()) {
                    executeAction();
                } else {
                    if (connectionHandler.isDatabaseInitialized()) {
                        if (connectionHandler.isAuthenticationProvided()) {
                            promptConnectDialog();
                        } else {
                            promptAuthenticationDialog();
                        }
                    } else {
                        promptDatabaseInitDialog();
                    }
                }
            } else {
                cancel();
            }
        } catch (ProcessCanceledException e) {
            // do nothing
        }

    }

    void promptDatabaseInitDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        getConnectionManager().promptDatabaseInitDialog(
                connectionHandler,
                new SimpleTask<Integer>() {
                    @Override
                    protected void execute() {
                        if (getOption() == 0) {
                            ConnectionInstructions instructions = connectionHandler.getInstructions();
                            instructions.setAllowAutoInit(true);
                            instructions.setAllowAutoConnect(true);
                            if (connectionHandler.isAuthenticationProvided()) {
                                executeAction();
                            } else {
                                promptAuthenticationDialog();
                            }
                        } else {
                            ConnectionAction.this.cancel();
                            cancel();
                        }
                    }
                });
    }

    void promptAuthenticationDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        getConnectionManager().promptAuthenticationDialog(
                connectionHandler,
                connectionHandler.getAuthenticationInfo().clone(),
                new SimpleTask<AuthenticationInfo>() {
                    @Override
                    protected void execute() {
                        AuthenticationInfo authenticationInfo = getOption();
                        if (authenticationInfo != null) {
                            executeAction();
                        } else {
                            ConnectionAction.this.cancel();
                            cancel();
                        }
                    }
                });
    }

    void promptConnectDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        getConnectionManager().promptConnectDialog(
                connectionHandler, description,
                new SimpleTask<Integer>() {
                    @Override
                    protected void execute() {
                        if (getOption() == 0) {
                            connectionHandler.getInstructions().setAllowAutoConnect(true);
                            executeAction();
                        } else {
                            ConnectionAction.this.cancel();
                            cancel();
                        }
                    }
                });
    }

    private ConnectionManager getConnectionManager() {
        return ConnectionManager.getInstance(getProject());
    }

    private void executeAction() {
        if (taskInstructions == null) {
            execute();
        } else {
            new BackgroundTask(getProject(), taskInstructions) {
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
