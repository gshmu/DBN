package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.connection.context.DatabaseContext;
import com.dci.intellij.dbn.connection.context.DatabaseContextBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;

public abstract class ConnectionAction implements DatabaseContextBase, Runnable{
    static final String[] OPTIONS_CONNECT_CANCEL = Commons.list("Connect", "Cancel");

    private final String description;
    private final boolean interactive;
    private final DatabaseContext context;
    private boolean cancelled;

    private ConnectionAction(String description, boolean interactive, DatabaseContext context) {
        this.description = description;
        this.interactive = interactive;
        this.context = context;
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
                    guarded(() -> run());
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
                            guarded(() -> run());
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
                        guarded(() -> run());
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
                        guarded(() -> run());
                    } else {
                        cancel();
                    }
                });
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return nd(this.context).ensureConnection();
    }

    public static void invoke(
            String description,
            boolean interactive,
            DatabaseContext databaseContext,
            Consumer<ConnectionAction> action) {
        new ConnectionAction(description, interactive, databaseContext) {
            @Override
            public void run() {
                guarded(() -> action.accept(this));
            }
        }.start();
    }

    public static void invoke(
            String description,
            boolean interactive,
            DatabaseContext databaseContext,
            Consumer<ConnectionAction> action,
            Consumer<ConnectionAction> cancel,
            Function<ConnectionAction, Boolean> canExecute) {

        new ConnectionAction(description, interactive, databaseContext) {
            @Override
            public void run() {
                if (canExecute == null || canExecute.apply(this)) {
                    guarded(() -> action.accept(this));
                }
            }

            @Override
            protected void cancel() {
                super.cancel();
                if (cancel != null){
                    cancel.accept(this);
                }
            }

        }.start();
    }


}
