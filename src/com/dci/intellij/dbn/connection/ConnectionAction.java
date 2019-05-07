package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class ConnectionAction implements Runnable{
    static final String[] OPTIONS_CONNECT_CANCEL = CommonUtil.list("Connect", "Cancel");

    private String description;
    private boolean interactive;
    private ConnectionProvider connectionProvider;
    private boolean cancelled;

    private ConnectionAction(String description, boolean interactive, ConnectionProvider connectionProvider) {
        this.description = description;
        this.interactive = interactive;
        this.connectionProvider = connectionProvider;
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    public boolean isCancelled() {
        if (cancelled) {
            return true;
        } else {
            ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();
            return progressIndicator != null && progressIndicator.isCanceled();
        }
    }

    protected void cancel() {
        cancelled = true;
    }

    public final void start() {
        Dispatch.invoke(() -> {
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler.isVirtual() || connectionHandler.canConnect()) {
                if (interactive || connectionHandler.isValid()) {
                    run();
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
                            run();
                        } else {
                            promptAuthenticationDialog();
                        }
                    } else {
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
                        run();
                    } else {
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
                        run();
                    } else {
                        cancel();
                    }
                });
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        ConnectionHandler connectionHandler = connectionProvider.getConnectionHandler();
        return Failsafe.nn(connectionHandler);
    }

    public static void invoke(
            String description,
            boolean interactive,
            ConnectionProvider connectionProvider,
            ParametricRunnable.Basic<ConnectionAction> action) {
        new ConnectionAction(description, interactive, connectionProvider) {
            @Override
            public void run() {
                action.run(this);
            }
        }.start();
    }

    public static <T extends Throwable> void invoke(
            String description,
            boolean interactive,
            ConnectionProvider connectionProvider,
            ParametricRunnable.Basic<ConnectionAction> action,
            ParametricRunnable.Basic<ConnectionAction> cancel,
            ParametricCallable.Basic<ConnectionAction, Boolean> canExecute) {

        new ConnectionAction(description, interactive, connectionProvider) {
            @Override
            public void run() {
                if (canExecute == null || canExecute.call(this)) {
                    action.run(this);
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
