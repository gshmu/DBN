package com.dci.intellij.dbn.debugger.jdwp.process;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.DBDebugOperationTask;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatusHolder;
import com.dci.intellij.dbn.debugger.jdwp.DBJdwpBreakpointHandler;
import com.dci.intellij.dbn.debugger.jdwp.ManagedThreadCommand;
import com.dci.intellij.dbn.debugger.jdwp.frame.DBJdwpDebugStackFrame;
import com.dci.intellij.dbn.debugger.jdwp.frame.DBJdwpDebugSuspendContext;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.engine.DebugProcessAdapter;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.debugger.engine.SuspendContext;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerContextListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionAdapter;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import com.sun.jdi.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Inet4Address;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;

import static com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus.BREAKPOINT_SETTING_ALLOWED;
import static com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus.DEBUGGER_STOPPING;
import static com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus.SESSION_INITIALIZATION_THREW_EXCEPTION;
import static com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus.TARGET_EXECUTION_STARTED;
import static com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus.TARGET_EXECUTION_TERMINATED;
import static com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus.TARGET_EXECUTION_THREW_EXCEPTION;

public abstract class DBJdwpDebugProcess<T extends ExecutionInput> extends JavaDebugProcess implements DBDebugProcess {
    public static final Key<DBJdwpDebugProcess> KEY = new Key<DBJdwpDebugProcess>("DBNavigator.JdwpDebugProcess");
    protected DBNConnection targetConnection;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBDebugProcessStatusHolder status = new DBDebugProcessStatusHolder();
    private int localTcpPort = 4000;

    private DBBreakpointHandler<DBJdwpDebugProcess>[] breakpointHandlers;
    private DBDebugConsoleLogger console;

    private transient XSuspendContext lastSuspendContext;

    protected DBJdwpDebugProcess(@NotNull final XDebugSession session, DebuggerSession debuggerSession, ConnectionHandler connectionHandler, int tcpPort) {
        super(session, debuggerSession);
        console = new DBDebugConsoleLogger(session);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        debuggerManager.registerDebugSession(connectionHandler);

        DBJdwpBreakpointHandler breakpointHandler = new DBJdwpBreakpointHandler(session, this);
        breakpointHandlers = new DBBreakpointHandler[]{breakpointHandler};
        localTcpPort = tcpPort;
        debuggerSession.getProcess().putUserData(KEY, this);
    }

    @Override
    public boolean set(DBDebugProcessStatus status, boolean value) {
        return this.status.set(status, value);
    }

    @Override
    public boolean is(DBDebugProcessStatus status) {
        return this.status.is(status);
    }

    @Override
    public boolean isNot(DBDebugProcessStatus status) {
        return this.status.isNot(status);
    }

    protected boolean shouldSuspend(XSuspendContext suspendContext) {
        if (is(TARGET_EXECUTION_TERMINATED)) {
            return false;
        } else {
            XExecutionStack executionStack = suspendContext.getActiveExecutionStack();
            if (executionStack != null) {
                XStackFrame topFrame = executionStack.getTopFrame();
                if (topFrame instanceof DBJdwpDebugStackFrame) {
                    return true;
                }
                Location location = getLocation(topFrame);
                VirtualFile virtualFile = getVirtualFile(location);
                return virtualFile != null;
            }
        }
        return true;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Nullable
    public T getExecutionInput() {
        DBRunConfig<T> runProfile = getRunProfile();
        return runProfile == null ? null : runProfile.getExecutionInput();
    }

    DBRunConfig<T> getRunProfile() {
        return (DBRunConfig<T>) getSession().getRunProfile();
    }

    @NotNull
    public Project getProject() {
        return getSession().getProject();
    }

    public DatabaseDebuggerInterface getDebuggerInterface() {
        return getConnectionHandler().getInterfaceProvider().getDebuggerInterface();
    }

    public DBNConnection getTargetConnection() {
        return targetConnection;
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
        return is(BREAKPOINT_SETTING_ALLOWED);
    }

    public DBDebugConsoleLogger getConsole() {
        return console;
    }

    @Override
    public void sessionInitialized() {
        final XDebugSession session = getSession();
        if (session instanceof XDebugSessionImpl) {
            XDebugSessionImpl sessionImpl = (XDebugSessionImpl) session;
            sessionImpl.getSessionData().setBreakpointsMuted(false);
        }
        DBRunConfig<T> runProfile = getRunProfile();
        List<DBMethod> methods = runProfile.getMethods();
        if (methods.size() > 0) {
            getBreakpointHandler().registerDefaultBreakpoint(methods.get(0));
        }

        DebuggerSession debuggerSession = getDebuggerSession();
        final Project project = getProject();
        DebuggerManager debuggerManager = DebuggerManager.getInstance(project);
        ProcessHandler processHandler = debuggerSession.getProcess().getProcessHandler();
        debuggerManager.addDebugProcessListener(processHandler, new DebugProcessAdapter(){
            @Override
            public void paused(SuspendContext suspendContext) {
                if (suspendContext instanceof XSuspendContext) {
                    XSuspendContext xSuspendContext = (XSuspendContext) suspendContext;

                    XExecutionStack[] executionStacks = xSuspendContext.getExecutionStacks();
                    for (XExecutionStack executionStack : executionStacks) {
                        System.out.println();
                    }

                    //underlyingFrame.getDescriptor().getLocation()

                }
            }
        });

        session.addSessionListener(new XDebugSessionAdapter() {
            @Override
            public void sessionPaused() {
                XSuspendContext suspendContext = session.getSuspendContext();
                if (!shouldSuspend(suspendContext)) {
                    new SimpleLaterInvocator() {
                        @Override
                        protected void execute() {
                            session.resume();
                        }
                    }.start();
                } else {
                    XExecutionStack activeExecutionStack = suspendContext.getActiveExecutionStack();
                    if (activeExecutionStack != null) {
                        XStackFrame topFrame = activeExecutionStack.getTopFrame();
                        if (topFrame instanceof JavaStackFrame) {
                            Location location = getLocation(topFrame);
                            VirtualFile virtualFile = getVirtualFile(location);
                            DBDebugUtil.openEditor(virtualFile);
                        }
                    }
                }
            }
        });

        debuggerSession.getContextManager().addListener(new DebuggerContextListener() {
            @Override
            public void changeEvent(DebuggerContextImpl newContext, DebuggerSession.Event event) {
                SuspendContextImpl suspendContext = newContext.getSuspendContext();
                overwriteSuspendContext(suspendContext);
            }
        });

        getDebuggerSession().getProcess().setXDebugProcess(this);

        new DBDebugOperationTask(project, "initialize debug environment") {
            public void execute() {
                try {
                    T executionInput = getExecutionInput();
                    if (executionInput != null) {
                        ConnectionHandler connectionHandler = getConnectionHandler();
                        targetConnection = connectionHandler.getPoolConnection(executionInput.getExecutionContext().getTargetSchema(), false);
                        targetConnection.setAutoCommit(false);
                        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                        debuggerInterface.initializeJdwpSession(targetConnection, Inet4Address.getLocalHost().getHostAddress(), String.valueOf(localTcpPort));
                        console.system("Debug session initialized (JDWP)");
                        set(BREAKPOINT_SETTING_ALLOWED, true);

                        initializeBreakpoints();
                        startTargetProgram();
                    }
                } catch (Exception e) {
                    set(SESSION_INITIALIZATION_THREW_EXCEPTION, true);
                    stop();
                    NotificationUtil.sendErrorNotification(project, "Error initializing debug environment.", e.getMessage());
                }
            }
        }.start();
    }

    private void initializeBreakpoints() {
        console.system("Registering breakpoints");
        List<DBMethod> methods = getRunProfile().getMethods();
        List<XLineBreakpoint<XBreakpointProperties>> breakpoints = DBBreakpointUtil.getDatabaseBreakpoints(getConnectionHandler());
        getBreakpointHandler().registerBreakpoints(breakpoints, methods);
    }

    private void overwriteSuspendContext(final @Nullable XSuspendContext suspendContext) {
        if (suspendContext != null && suspendContext != lastSuspendContext && !(suspendContext instanceof DBJdwpDebugSuspendContext)) {
            lastSuspendContext = suspendContext;
            final XDebugSession session = getSession();
            if (shouldSuspend(suspendContext)) {
                new ManagedThreadCommand(getDebuggerSession().getProcess()) {
                    @Override
                    protected void action() throws Exception {
                        DBJdwpDebugSuspendContext dbSuspendContext = new DBJdwpDebugSuspendContext(DBJdwpDebugProcess.this, suspendContext);
                        session.positionReached(dbSuspendContext);
                    }
                }.schedule();
                throw AlreadyDisposedException.INSTANCE;
            }
        }
    }

    private void startTargetProgram() {
        // trigger in managed thread
        new ManagedThreadCommand(getDebuggerSession().getProcess()) {
            @Override
            protected void action() throws Exception {
                new BackgroundTask(getProject(), "Running debugger target program", true, true) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                        T executionInput = getExecutionInput();
                        progressIndicator.setText("Executing " + (executionInput == null ? " target program" : executionInput.getExecutionContext().getTargetName()));
                        console.system("Executing target program");
                        if (is(SESSION_INITIALIZATION_THREW_EXCEPTION)) return;
                        try {
                            set(TARGET_EXECUTION_STARTED, true);
                            executeTarget();
                        } catch (SQLException e){
                            set(TARGET_EXECUTION_THREW_EXCEPTION, true);
                            if (isNot(DEBUGGER_STOPPING)) {
                                String message = executionInput == null ? "Error executing target program" : "Error executing " + executionInput.getExecutionContext().getTargetName();
                                console.error(message + ": " + e.getMessage());
                            }
                        } finally {
                            set(TARGET_EXECUTION_TERMINATED, true);
                            stop();
                        }
                    }
                }.start();
            }
        }.schedule();
    }

    protected abstract void executeTarget() throws SQLException;

    @Override
    public synchronized void stop() {
        if (isNot(DEBUGGER_STOPPING)) {
            set(DEBUGGER_STOPPING, true);
            set(BREAKPOINT_SETTING_ALLOWED, false);
            console.system("Stopping debugger...");
            super.stop();
            stopDebugger();
        }
    }

    private void stopDebugger() {
        final Project project = getProject();
        new BackgroundTask(project, "Stopping debugger", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setText("Stopping debug environment.");
                T executionInput = getExecutionInput();
                if (executionInput != null && isNot(TARGET_EXECUTION_TERMINATED)) {
                    ExecutionContext context = executionInput.getExecutionContext();
                    ConnectionUtil.cancel(context.getStatement());
                }


                ConnectionHandler connectionHandler = getConnectionHandler();
                try {
                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    debuggerInterface.disconnectJdwpSession(targetConnection);

                } catch (final SQLException e) {
                    NotificationUtil.sendErrorNotification(getProject(), "Error stopping debugger.", e.getMessage());
                    //showErrorDialog(e);
                } finally {
                    DBRunConfig<T> runProfile = getRunProfile();
                    if (runProfile != null && runProfile.getCategory() != DBRunConfigCategory.CUSTOM) {
                        runProfile.setCanRun(false);
                    }

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

    @Nullable
    public VirtualFile getVirtualFile(Location location) {
        try {
            if (location != null) {
                String sourcePath = location.sourcePath();
                StringTokenizer tokenizer = new StringTokenizer(sourcePath, "\\.");
                tokenizer.nextToken(); // signature
                String programType = tokenizer.nextToken();// program type
                if (!programType.equals("Block")) {
                    String schemaName = tokenizer.nextToken();
                    String programName = tokenizer.nextToken();
                    DBSchema schema = getConnectionHandler().getObjectBundle().getSchema(schemaName);
                    if (schema != null) {
                        DBProgram program = schema.getProgram(programName);
                        if (program != null) {
                            return program.getVirtualFile();
                        } else {
                            DBMethod method = schema.getMethod(programName, 0);
                            if (method != null) {
                                return method.getVirtualFile();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            getConsole().error("Error evaluating suspend position: " + e.getMessage());
        }

        return null;
    }

    @Nullable
    public String getOwnerName(@Nullable Location location) {
        try {
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

    @Nullable
    private Location getLocation(@Nullable XStackFrame stackFrame) {
        if (stackFrame instanceof JavaStackFrame) {
            return ((JavaStackFrame) stackFrame).getDescriptor().getLocation();
        }
        return null;
    }
}
