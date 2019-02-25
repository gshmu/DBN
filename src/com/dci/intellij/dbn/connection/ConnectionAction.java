package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricCallback;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class ConnectionAction extends SimpleTask<Integer> {
    public static final String[] OPTIONS_CONNECT_CANCEL = new String[]{"Connect", "Cancel"};

    private String description;
    private boolean interactive;
    private ConnectionProvider connectionProvider;
    private Integer executeOption;
    protected ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();

    private ConnectionAction(String description, boolean interactive, ConnectionProvider connectionProvider) {
        this(description, interactive, connectionProvider, null);
    }

    public ConnectionAction(String description, boolean interactive, ConnectionProvider connectionProvider, Integer executeOption) {
        this.description = description;
        this.interactive = interactive;
        this.connectionProvider = connectionProvider;
        this.executeOption = executeOption;
    }

    @Override
    protected boolean canExecute() {
        return executeOption == null || executeOption.equals(getData());
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
        Dispatch.invoke(() -> ConnectionAction.super.start());
    }

    @Override
    public final void run() {
        trace(this);
        Failsafe.lenient(() -> {
            if (canExecute()) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler.isVirtual() || connectionHandler.canConnect()) {
                    if (interactive || connectionHandler.isValid()) {
                        execute();
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
                (option) -> {
                    if (option == 0) {
                        ConnectionInstructions instructions = connectionHandler.getInstructions();
                        instructions.setAllowAutoInit(true);
                        instructions.setAllowAutoConnect(true);
                        if (connectionHandler.isAuthenticationProvided()) {
                            execute();
                        } else {
                            promptAuthenticationDialog();
                        }
                    } else {
                        ConnectionAction.this.cancel();
                        cancel();
                    }
                });
    }

    private void promptAuthenticationDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        AuthenticationInfo temporaryAuthenticationInfo = connectionHandler.getAuthenticationInfo().clone();
        temporaryAuthenticationInfo.setTemporary(true);
        ConnectionManager.promptAuthenticationDialog(
                connectionHandler,
                temporaryAuthenticationInfo,
                (authenticationInfo) -> {
                    if (authenticationInfo != null) {
                        execute();
                    } else {
                        ConnectionAction.this.cancel();
                        cancel();
                    }
                });
    }

    private void promptConnectDialog() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionManager.promptConnectDialog(
                connectionHandler,
                description,
                (option) -> {
                    if (option == 0) {
                        connectionHandler.getInstructions().setAllowAutoConnect(true);
                        execute();
                    } else {
                        ConnectionAction.this.cancel();
                        cancel();
                    }
                });
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
            boolean interactive,
            ConnectionProvider connectionProvider,
            ParametricCallback<ConnectionAction> action) {
        create(description, interactive, connectionProvider, null, action).start();
    }

    @Deprecated
    public static ConnectionAction create(
            String description,
            boolean interactive,
            ConnectionProvider connectionProvider,
            Integer executeOption,
            ParametricCallback<ConnectionAction> action) {
        return new ConnectionAction(description, interactive, connectionProvider, executeOption) {
            @Override
            protected void execute() {
                action.run(this);
            }
        };
    }

    public static void invoke(
            String description,
            boolean interactive,
            ConnectionProvider connectionProvider,
            ParametricCallback<ConnectionAction> action,
            ParametricCallback<ConnectionAction> cancel,
            ParametricCallable.Unsafe<ConnectionAction, Boolean> canExecute) {

        create(description, interactive, connectionProvider, action, cancel, canExecute).start();
    }

    private static ConnectionAction create(
            String description,
            boolean interactive,
            ConnectionProvider connectionProvider,
            ParametricCallback<ConnectionAction> action,
            ParametricCallback<ConnectionAction> cancel,
            ParametricCallable.Unsafe<ConnectionAction, Boolean> canExecute) {

        return new ConnectionAction(description, interactive, connectionProvider) {
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
}
