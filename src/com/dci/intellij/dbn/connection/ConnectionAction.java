package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.BackgroundMonitor;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class ConnectionAction extends SimpleTask<Integer> {
    public static final String[] OPTIONS_CONNECT_CANCEL = new String[]{"Connect", "Cancel"};

    private String description;
    private ConnectionProvider connectionProvider;
    private TaskInstructions taskInstructions;
    private Integer executeOption;
    protected ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();

    private ConnectionAction(String description, ConnectionProvider connectionProvider, Integer executeOption) {
        this.description = description;
        this.connectionProvider = connectionProvider;
        this.executeOption = executeOption;
    }

    private ConnectionAction(String description, ConnectionProvider connectionProvider, TaskInstructions taskInstructions) {
        this(description, connectionProvider, taskInstructions, null);
    }

    public ConnectionAction(String description, ConnectionProvider connectionProvider, TaskInstructions taskInstructions, Integer executeOption) {
        this.description = description;
        this.connectionProvider = connectionProvider;
        this.taskInstructions = taskInstructions;
        this.executeOption = executeOption;
    }

    @Override
    protected boolean canExecute() {
        return executeOption == null || executeOption.equals(getData());
    }

    protected boolean isManaged() {
        return taskInstructions != null && taskInstructions.is(TaskInstruction.MANAGED);
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Override
    public boolean isCancelled() {
        if (super.isCancelled()) {
            return true;
        } else {
            ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();
            return progressIndicator != null && progressIndicator.isCanceled();
        }
    }

    @Override
    public final void start() {
        SimpleLaterInvocator.invoke(() -> ConnectionAction.super.start());
    }

    @Override
    public final void run() {
        trace(this);
        Failsafe.lenient(() -> {
            if (canExecute()) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler.isVirtual() || connectionHandler.canConnect()) {
                    if (isManaged() || connectionHandler.isValid()) {
                        executeAction();
                    } else {
                        String connectionName = connectionHandler.getName();
                        Throwable connectionException = connectionHandler.getConnectionStatus().getConnectionException();
                        ConnectionManager.showErrorConnectionMessage(getProject(), connectionName, connectionException);
                    }
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
        });
    }

    private void promptDatabaseInitDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.promptDatabaseInitDialog(
                connectionHandler,
                MessageCallback.create(null, option -> {
                    if (option == 0) {
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
                }));
    }

    private void promptAuthenticationDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        AuthenticationInfo temporaryAuthenticationInfo = connectionHandler.getAuthenticationInfo().clone();
        temporaryAuthenticationInfo.setTemporary(true);
        ConnectionManager.promptAuthenticationDialog(
                connectionHandler,
                temporaryAuthenticationInfo,
                SimpleTask.create(authenticationInfo -> {
                    if (authenticationInfo != null) {
                        executeAction();
                    } else {
                        ConnectionAction.this.cancel();
                        cancel();
                    }
                }));
    }

    private void promptConnectDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.promptConnectDialog(
                connectionHandler,
                description,
                MessageCallback.create(null, option -> {
                    if (option == 0) {
                        connectionHandler.getInstructions().setAllowAutoConnect(true);
                        executeAction();
                    } else {
                        ConnectionAction.this.cancel();
                        cancel();
                    }
                }));
    }

    private void executeAction() {
        if (taskInstructions == null || BackgroundMonitor.isBackgroundProcess()) {
            if (!ProgressMonitor.isCancelled()) {
                execute();
            }
        } else {
            BackgroundTask.invoke(
                    getProject(),
                    taskInstructions,
                    (task, progress) -> ConnectionAction.this.execute());
        }
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        ConnectionHandler connectionHandler = connectionProvider.getConnectionHandler();
        return Failsafe.get(connectionHandler);
    }

    @Override
    protected abstract void execute();

    public static void invoke(
            String description,
            ConnectionProvider connectionProvider,
            ParametricRunnable.Unsafe<ConnectionAction> action) {
        create(description, connectionProvider, null, action).start();
    }

    public static ConnectionAction create(
            String description,
            ConnectionProvider connectionProvider,
            Integer executeOption,
            ParametricRunnable.Unsafe<ConnectionAction> action) {
        return new ConnectionAction(description, connectionProvider, executeOption) {
            @Override
            protected void execute() {
                action.run(this);
            }
        };
    }

    public static void invoke(
            String description,
            TaskInstructions taskInstructions,
            ConnectionProvider connectionProvider,
            ParametricRunnable.Unsafe<ConnectionAction> runnable) {
        create(description, taskInstructions, connectionProvider, runnable, null, null).start();
    }

    public static ConnectionAction create(
            String description,
            TaskInstructions taskInstructions,
            ConnectionProvider connectionProvider,
            ParametricRunnable.Unsafe<ConnectionAction> action) {
        return create(description, taskInstructions, connectionProvider, action, null, null);
    }

    public static void invoke(
            String description,
            TaskInstructions taskInstructions,
            ConnectionProvider connectionProvider,
            ParametricRunnable.Unsafe<ConnectionAction> action,
            ParametricRunnable.Unsafe<ConnectionAction> cancel,
            Callable<Boolean> canExecute) {

        create(description, taskInstructions, connectionProvider, action, cancel, canExecute).start();
    }

    public static ConnectionAction create(
            String description,
            TaskInstructions taskInstructions,
            ConnectionProvider connectionProvider,
            ParametricRunnable.Unsafe<ConnectionAction> action,
            ParametricRunnable.Unsafe<ConnectionAction> cancel,
            Callable<Boolean> canExecute) {

        return new ConnectionAction(description, connectionProvider, taskInstructions) {
            @Override
            protected void execute() {
                ProgressMonitor.invoke(progressIndicator, () -> action.run(this));
            }

            @Override
            protected void cancel() {
                super.cancel();
                if (cancel != null){
                    cancel.run(this);
                }
            }

            @Override
            protected boolean canExecute() {
                if (canExecute != null) {
                    return canExecute.call(this);
                } else {
                    return super.canExecute();
                }
            }
        };
    }
    @FunctionalInterface
    public interface Runnable {
        void run(ConnectionAction action);
    }

    public interface Callable<V> {
        V call(ConnectionAction action);
    }
}
