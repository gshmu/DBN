package com.dci.intellij.dbn.debugger.jdwp.process;

import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.DebugOperationThread;
import com.dci.intellij.dbn.debugger.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.jdbc.process.DBProgramDebugProcessStatus;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DBProgramJdwpDebugProcess<T extends ExecutionInput> extends JavaDebugProcess implements Presentable {
    protected Connection targetConnection;
    private T executionInput;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBProgramDebugProcessStatus status = new DBProgramDebugProcessStatus();

    protected DBProgramJdwpDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession, ConnectionHandler connectionHandler) {
        super(session, debuggerSession);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        debuggerManager.registerDebugSession(connectionHandler);

        DBProgramRunConfiguration<T> runProfile = (DBProgramRunConfiguration) session.getRunProfile();
        executionInput = runProfile.getExecutionInput();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    public T getExecutionInput() {
        return executionInput;
    }

    @NotNull
    public Project getProject() {
        return getSession().getProject();
    }

    public DatabaseDebuggerInterface getDebuggerInterface() {
        return getConnectionHandler().getInterfaceProvider().getDebuggerInterface();
    }

    public Connection getTargetConnection() {
        return targetConnection;
    }

    @Override
    public void sessionInitialized() {
        final Project project = getProject();
        new BackgroundTask(project, "Initialize debug environment", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                try {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    targetConnection = connectionHandler.getPoolConnection(executionInput.getExecutionContext().getTargetSchema());
                    targetConnection.setAutoCommit(false);

                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    progressIndicator.setText("Initializing debugger target session");
                    debuggerInterface.initializeJdwpSession(targetConnection, Inet4Address.getLocalHost().getHostAddress(), "4000");

                    executeTarget();
                } catch (Exception e) {
                    status.SESSION_INITIALIZATION_THREW_EXCEPTION = true;
                    getSession().stop();
                    NotificationUtil.sendErrorNotification(getProject(), "Error initializing debug environment.", e.getMessage());
                }
            }
        }.start();
    }

    private void executeTarget() {
        final Project project = getProject();
        new DebugOperationThread(project, "execute target program") {
            public void executeOperation() throws SQLException {

                if (status.PROCESS_IS_TERMINATING) return;
                if (status.SESSION_INITIALIZATION_THREW_EXCEPTION) return;
                try {
                    status.TARGET_EXECUTION_STARTED = true;
                    doExecuteTarget();
                } catch (SQLException e){
                    status.TARGET_EXECUTION_THREW_EXCEPTION = true;
                    MessageUtil.showErrorDialog(project, "Error executing " + executionInput.getExecutionContext().getTargetName(), e);
                } finally {
                    status.TARGET_EXECUTION_TERMINATED = true;
                    getSession().stop();
                }
            }
        }.start();
    }

    protected abstract void doExecuteTarget() throws SQLException;

    @Override
    public void stop() {
        super.stop();
        Project project = getSession().getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        ConnectionHandler connectionHandler = getConnectionHandler();
        debuggerManager.unregisterDebugSession(connectionHandler);
    }

    private void stopDebugger() {
        final Project project = getProject();
        new BackgroundTask(project, "Stopping debugger", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setText("Stopping debug environment.");
                ConnectionHandler connectionHandler = getConnectionHandler();
                try {
                    status.CAN_SET_BREAKPOINTS = false;
                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();

                    debuggerInterface.disconnectJdwpSession(targetConnection);
                } catch (final SQLException e) {
                    NotificationUtil.sendErrorNotification(getProject(), "Error stopping debugger.", e.getMessage());
                    //showErrorDialog(e);
                } finally {
                    status.PROCESS_IS_TERMINATED = true;
                    releaseTargetConnection();
                    DatabaseDebuggerManager.getInstance(project).unregisterDebugSession(connectionHandler);
                }
            }
        }.start();
    }

    protected void releaseTargetConnection() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.dropPoolConnection(targetConnection);
        targetConnection = null;
    }

}
