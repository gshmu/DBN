package com.dci.intellij.dbn.debugger.jdwp;

import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.DBDebugOperationTask;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

public abstract class DBJdwpDebugProcess<T extends ExecutionInput> extends JavaDebugProcess implements DBDebugProcess {
    protected Connection targetConnection;
    private T executionInput;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBDebugProcessStatus status = new DBDebugProcessStatus();

    private DBJdwpBreakpointHandler breakpointHandler;
    private DBJdwpBreakpointHandler[] breakpointHandlers;
    private DBDebugConsoleLogger console;


    protected DBJdwpDebugProcess(@NotNull XDebugSession session, DebuggerSession debuggerSession, ConnectionHandler connectionHandler) {
        super(session, debuggerSession);
        console = new DBDebugConsoleLogger(session);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        debuggerManager.registerDebugSession(connectionHandler);

        DBProgramRunConfiguration<T> runProfile = (DBProgramRunConfiguration<T>) session.getRunProfile();
        executionInput = runProfile.getExecutionInput();

        breakpointHandler = new DBJdwpBreakpointHandler(session, this);
        breakpointHandlers = new DBJdwpBreakpointHandler[]{breakpointHandler};
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

    public DBDebugProcessStatus getStatus() {
        return status;
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return breakpointHandlers;
    }

    @Override
    public boolean checkCanInitBreakpoints() {
        return status.CAN_SET_BREAKPOINTS;
    }

    public DBDebugConsoleLogger getConsole() {
        return console;
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
                    getSession().getConsoleView().print("JWDP Session initialized", ConsoleViewContentType.SYSTEM_OUTPUT);

                    status.CAN_SET_BREAKPOINTS = true;
                    registerBreakpoints(new ExecuteTargetTask());
                } catch (Exception e) {
                    status.SESSION_INITIALIZATION_THREW_EXCEPTION = true;
                    getSession().stop();
                    NotificationUtil.sendErrorNotification(getProject(), "Error initializing debug environment.", e.getMessage());
                }
            }
        }.start();
    }

    private void registerBreakpoints(final RunnableTask callback) {
        final Collection<XLineBreakpoint> breakpoints = new ReadActionRunner<Collection<XLineBreakpoint>>() {
            @Override
            protected Collection<XLineBreakpoint> run() {
                XBreakpointType localXBreakpointType = XDebuggerUtil.getInstance().findBreakpointType(breakpointHandler.getBreakpointTypeClass());
                Project project = getProject();
                XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
                return breakpointManager.getBreakpoints(localXBreakpointType);
            }
        }.start();

        new WriteActionRunner() {
            @Override
            public void run() {
                for (XLineBreakpoint breakpoint : breakpoints) {
                    breakpointHandler.registerBreakpoint(breakpoint);
                }

                registerDefaultBreakpoint();
                callback.start();
            }
        }.start();
    }

    protected abstract void registerDefaultBreakpoint();

    protected void registerDefaultBreakpoint(DBMethod method) {
        DBEditableObjectVirtualFile mainDatabaseFile = DBDebugUtil.getMainDatabaseFile(method);
        if (mainDatabaseFile != null) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) mainDatabaseFile.getMainContentFile();
            PSQLFile psqlFile = (PSQLFile) sourceCodeFile.getPsiFile();
            if (psqlFile != null) {
                BasePsiElement basePsiElement = psqlFile.lookupObjectDeclaration(method.getObjectType().getGenericType(), method.getName());
                if (basePsiElement != null) {
                    BasePsiElement subject = basePsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    int offset = subject.getTextOffset();
                    Document document = DocumentUtil.getDocument(psqlFile);
                    int line = document.getLineNumber(offset);

                    DBSchemaObject schemaObject = DBDebugUtil.getMainDatabaseObject(method);
                    if (schemaObject != null) {
                        try {
/*
                            defaultBreakpointInfo = getDebuggerInterface().addProgramBreakpoint(
                                    method.getSchema().getName(),
                                    schemaObject.getName(),
                                    schemaObject.getObjectType().getName().toUpperCase(),
                                    line,
                                    getDebugConnection());
*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class ExecuteTargetTask extends DBDebugOperationTask {
        public ExecuteTargetTask() {
            super(getProject(), "execute method");
        }

        public void execute() throws SQLException {
            if (status.PROCESS_IS_TERMINATING) return;
            if (status.SESSION_INITIALIZATION_THREW_EXCEPTION) return;
            try {
                status.TARGET_EXECUTION_STARTED = true;
                doExecuteTarget();
            } catch (SQLException e){
                status.TARGET_EXECUTION_THREW_EXCEPTION = true;
                MessageUtil.showErrorDialog(getProject(), "Error executing " + executionInput.getExecutionContext().getTargetName(), e);
            } finally {
                status.TARGET_EXECUTION_TERMINATED = true;
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                debuggerInterface.disconnectJdwpSession(targetConnection);
            }
        }
    }

    protected abstract void doExecuteTarget() throws SQLException;

    @Override
    public void stop() {
        super.stop();
        stopDebugger();
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
                    if (!status.TARGET_EXECUTION_TERMINATED) {
                        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                        debuggerInterface.disconnectJdwpSession(targetConnection);
                    }

                } catch (final SQLException e) {
                    NotificationUtil.sendErrorNotification(getProject(), "Error stopping debugger.", e.getMessage());
                    //showErrorDialog(e);
                } finally {
                    status.PROCESS_IS_TERMINATED = true;
                    DatabaseDebuggerManager.getInstance(project).unregisterDebugSession(connectionHandler);
                    releaseTargetConnection();
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
