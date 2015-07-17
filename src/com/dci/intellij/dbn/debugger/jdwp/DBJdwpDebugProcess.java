package com.dci.intellij.dbn.debugger.jdwp;

import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.StringTokenizer;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.DBDebugOperationTask;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointType;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus;
import com.dci.intellij.dbn.debugger.jdwp.frame.DBJdwpDebugSuspendContext;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerContextListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionAdapter;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.sun.jdi.Location;

public abstract class DBJdwpDebugProcess<T extends ExecutionInput> extends JavaDebugProcess implements DBDebugProcess {
    protected Connection targetConnection;
    private T executionInput;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBDebugProcessStatus status = new DBDebugProcessStatus();

    private DBBreakpointHandler<DBJdwpDebugProcess>[] breakpointHandlers;
    private DBDebugConsoleLogger console;

    private transient XSuspendContext lastSuspendContext;

    private XDebugSessionListener suspendContextOverwriteListener = new XDebugSessionAdapter() {
        @Override
        public void sessionPaused() {
            final XDebugSession session = getSession();
            final XSuspendContext suspendContext = session.getSuspendContext();
            if (suspendContext instanceof DBJdwpDebugSuspendContext) {

            } else if (suspendContext != lastSuspendContext){
                lastSuspendContext = suspendContext;
                final DBJdwpDebugSuspendContext dbSuspendContext = new DBJdwpDebugSuspendContext(DBJdwpDebugProcess.this, suspendContext);
                session.positionReached(dbSuspendContext);
                throw AlreadyDisposedException.INSTANCE;
            }
        }
    };


    protected DBJdwpDebugProcess(@NotNull final XDebugSession session, DebuggerSession debuggerSession, ConnectionHandler connectionHandler) {
        super(session, debuggerSession);
        console = new DBDebugConsoleLogger(session);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        debuggerManager.registerDebugSession(connectionHandler);

        DBProgramRunConfiguration<T> runProfile = (DBProgramRunConfiguration<T>) session.getRunProfile();
        executionInput = runProfile.getExecutionInput();

        DBJdwpBreakpointHandler breakpointHandler = new DBJdwpBreakpointHandler(session, this);
        breakpointHandlers = new DBBreakpointHandler[]{breakpointHandler};

        getDebuggerSession().getContextManager().addListener(new DebuggerContextListener() {
            @Override
            public void changeEvent(DebuggerContextImpl newContext, int event) {
                //System.out.println();
            }
        });
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
    public DBBreakpointHandler<DBJdwpDebugProcess>[] getBreakpointHandlers() {
        return breakpointHandlers;
    }

    public DBBreakpointHandler<DBJdwpDebugProcess> getBreakpointHandler() {
        return breakpointHandlers[0];
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
        final XDebugSession session = getSession();
        session.addSessionListener(suspendContextOverwriteListener);
        getDebuggerSession().getProcess().setXDebugProcess(this);

        new DBDebugOperationTask(getProject(), "initialize debug environment") {
            public void execute() {
                try {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    targetConnection = connectionHandler.getPoolConnection(executionInput.getExecutionContext().getTargetSchema());
                    targetConnection.setAutoCommit(false);

                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    debuggerInterface.initializeJdwpSession(targetConnection, Inet4Address.getLocalHost().getHostAddress(), "4000");
                    console.system("Debug session initialized (JDWP)");

                    status.CAN_SET_BREAKPOINTS = true;
                    executeTargetProgram();
                } catch (Exception e) {
                    status.SESSION_INITIALIZATION_THREW_EXCEPTION = true;
                    session.stop();
                    NotificationUtil.sendErrorNotification(getProject(), "Error initializing debug environment.", e.getMessage());
                }
            }
        }.start();
    }

    private void registerBreakpoints() {
        console.system("Registering breakpoints...");
        final Collection<XLineBreakpoint<DBBreakpointProperties>> breakpoints = new ReadActionRunner<Collection<XLineBreakpoint<DBBreakpointProperties>>>() {
            @Override
            protected Collection<XLineBreakpoint<DBBreakpointProperties>> run() {
                DBBreakpointType localXBreakpointType = (DBBreakpointType) XDebuggerUtil.getInstance().findBreakpointType(DBBreakpointType.class);
                Project project = getProject();
                XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
                return (Collection<XLineBreakpoint<DBBreakpointProperties>>) breakpointManager.getBreakpoints(localXBreakpointType);
            }
        }.start();

        //breakpointHandler.registerBreakpoints(breakpoints);
        registerDefaultBreakpoint();
        console.system("Done registering breakpoints");
        executeTargetProgram();
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

    private void executeTargetProgram() {
        new DBDebugOperationTask(getProject(), "execute method") {
            public void execute() throws SQLException {
                console.system("Executing target program");
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
        }.start();
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

    public VirtualFile getVirtualFile(XStackFrame stackFrame) {
        try {
            Location location = ((JavaStackFrame) stackFrame).getDescriptor().getLocation();
            if (location != null) {
                int lineNumber = location.lineNumber();
                String sourcePath = location.sourcePath();
                StringTokenizer tokenizer = new StringTokenizer(sourcePath, "\\.");
                String signature = tokenizer.nextToken();
                String programType = tokenizer.nextToken();
                String schemaName = tokenizer.nextToken();
                String programName = tokenizer.nextToken();
                DBSchema schema = getConnectionHandler().getObjectBundle().getSchema(schemaName);
                if (schema != null) {
                    DBProgram program = schema.getProgram(programName);
                    if (program != null) {
                        DBEditableObjectVirtualFile virtualFile = program.getVirtualFile();
                        return virtualFile.getMainContentFile();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getOwnerName(XStackFrame stackFrame) {
        try {
            Location location = ((JavaStackFrame) stackFrame).getDescriptor().getLocation();
            if (location != null) {
                String sourcePath = location.sourcePath();
                StringTokenizer tokenizer = new StringTokenizer(sourcePath, "\\.");
                String signature = tokenizer.nextToken();
                String programType = tokenizer.nextToken();
                return tokenizer.nextToken();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
