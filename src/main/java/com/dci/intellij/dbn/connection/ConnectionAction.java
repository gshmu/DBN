package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Guarded;
import com.dci.intellij.dbn.connection.context.DatabaseContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class ConnectionAction implements Runnable{
    static final String[] OPTIONS_CONNECT_CANCEL = Commons.list("Connect", "Cancel");

    private final String description;
    private final boolean interactive;
    private final DatabaseContext connectionProvider;
    private boolean cancelled;

    private ConnectionAction(String description, boolean interactive, DatabaseContext connectionProvider) {
        this.description = description;
        this.interactive = interactive;
        this.connectionProvider = connectionProvider;
    }

    @NotNull
    public Project getProject() {
        return getConnection().getProject();
    }

    public boolean isCancelled() {
        if (cancelled) {
            return true;
        } else {
            return ProgressMonitor.isCancelled();
        }
    }

    protected void cancel() {
        cancelled = true;
    }

    public final void start() {
        Dispatch.run(() -> {
            ConnectionHandler connection = getConnection();
            if (connection.isVirtual() || connection.canConnect()) {
                if (interactive || connection.isValid()) {
                    Guarded.run(() -> run());
                } else {
                    String connectionName = connection.getName();
                    Throwable connectionException = connection.getConnectionStatus().getConnectionException();
                    ConnectionManager.showErrorConnectionMessage(getProject(), connectionName, connectionException);
                }
            } else {
                if (connection.isDatabaseInitialized()) {
                    if (connection.isAuthenticationProvided()) {
                        promptConnectDialog();
                    } else {
                        promptAuthenticationDialog();
                    }
                } else {
                    promptDatabaseInitDialog();
                }
            }
        });
    }

    private void promptDatabaseInitDialog() {
        ConnectionHandler connection = getConnection();
        ConnectionManager.promptDatabaseInitDialog(
                connection,
                option -> {
                    if (option == 0) {
                        ConnectionInstructions instructions = connection.getInstructions();
                        instructions.setAllowAutoInit(true);
                        instructions.setAllowAutoConnect(true);
                        if (connection.isAuthenticationProvided()) {
                            Guarded.run(() -> run());
                        } else {
                            promptAuthenticationDialog();
                        }
                    } else {
                        cancel();
                    }
                });
    }

    private void promptAuthenticationDialog() {
        ConnectionHandler connection = getConnection();
        AuthenticationInfo temporaryAuthenticationInfo = connection.getAuthenticationInfo().clone();
        temporaryAuthenticationInfo.setTemporary(true);
        ConnectionManager connectionManager = ConnectionManager.getInstance(connection.getProject());
        connectionManager.promptAuthenticationDialog(
                connection,
                temporaryAuthenticationInfo,
                authenticationInfo -> {
                    if (authenticationInfo != null) {
                        Guarded.run(() -> run());
                    } else {
                        cancel();
                    }
                });
    }

    private void promptConnectDialog() {
        ConnectionHandler connection = getConnection();
        ConnectionManager.promptConnectDialog(
                connection,
                description,
                option -> {
                    if (option == 0) {
                        connection.getInstructions().setAllowAutoConnect(true);
                        Guarded.run(() -> run());
                    } else {
                        cancel();
                    }
                });
    }

    @NotNull
    public ConnectionHandler getConnection() {
        ConnectionHandler connection = connectionProvider.getConnection();
        return Failsafe.nn(connection);
    }

    public static void invoke(
            String description,
            boolean interactive,
            DatabaseContext databaseContext,
            ParametricRunnable.Basic<ConnectionAction> action) {
        new ConnectionAction(description, interactive, databaseContext) {
            @Override
            public void run() {
                Guarded.run(() -> action.run(this));
            }
        }.start();
    }

    public static void invoke(
            String description,
            boolean interactive,
            DatabaseContext databaseContext,
            ParametricRunnable.Basic<ConnectionAction> action,
            ParametricRunnable.Basic<ConnectionAction> cancel,
            ParametricCallable.Basic<ConnectionAction, Boolean> canExecute) {

        new ConnectionAction(description, interactive, databaseContext) {
            @Override
            public void run() {
                if (canExecute == null || canExecute.call(this)) {
                    Guarded.run(() -> action.run(this));
                }
            }

            @Override
            protected void cancel() {
                super.cancel();
                if (cancel != null){
                    cancel.run(this);
                }
            }

        }.start();
    }


}
