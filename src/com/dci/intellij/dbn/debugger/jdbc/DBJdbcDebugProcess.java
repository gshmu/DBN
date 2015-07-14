package com.dci.intellij.dbn.debugger.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerSessionInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.DBDebugOperationTask;
import com.dci.intellij.dbn.debugger.DBDebugTabLayouter;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBProgramDebuggerEditorsProvider;
import com.dci.intellij.dbn.debugger.jdbc.frame.DBProgramDebugSuspendContext;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.ui.XDebugTabLayouter;

public abstract class DBJdbcDebugProcess<T extends ExecutionInput> extends XDebugProcess implements DBDebugProcess {
    protected Connection targetConnection;
    protected Connection debugConnection;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBJdbcBreakpointHandler breakpointHandler;
    private DBJdbcBreakpointHandler[] breakpointHandlers;
    private T executionInput;
    protected BreakpointInfo defaultBreakpointInfo;
    private DBDebugProcessStatus status = new DBDebugProcessStatus();

    private transient DebuggerRuntimeInfo runtimeInfo;
    private transient ExecutionBacktraceInfo backtraceInfo;
    private DBDebugConsoleLogger console;


    public DBJdbcDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session);
        console = new DBDebugConsoleLogger(session);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager.getInstance(project).registerDebugSession(connectionHandler);

        DBProgramRunConfiguration<T> runProfile = (DBProgramRunConfiguration) session.getRunProfile();
        executionInput = runProfile.getExecutionInput();

        breakpointHandler = new DBJdbcBreakpointHandler(session, this);
        breakpointHandlers = new DBJdbcBreakpointHandler[]{breakpointHandler};
    }

    public DBDebugProcessStatus getStatus() {
        return status;
    }

    public Connection getTargetConnection() {
        return targetConnection;
    }

    public Connection getDebugConnection() {
        return debugConnection;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @NotNull
    public Project getProject() {
        return getSession().getProject();
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return breakpointHandlers;
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return DBProgramDebuggerEditorsProvider.INSTANCE;
    }

    public T getExecutionInput() {
        return executionInput;
    }

    @Override
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
                    console.system("Initializing debug environment...");
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    targetConnection = connectionHandler.getPoolConnection(executionInput.getExecutionContext().getTargetSchema());
                    targetConnection.setAutoCommit(false);
                    debugConnection = connectionHandler.getPoolConnection();
                    console.system("Debug connections allocated");

                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    progressIndicator.setText("Initializing debugger target session");

                    DebuggerSessionInfo sessionInfo = debuggerInterface.initializeSession(targetConnection);
                    console.system("Debug target session initialized");
                    debuggerInterface.enableDebugging(targetConnection);
                    debuggerInterface.attachSession(sessionInfo.getSessionId(), debugConnection);
                    console.system("Attached debug session");

                    synchronizeSession();
                } catch (SQLException e) {
                    status.SESSION_INITIALIZATION_THREW_EXCEPTION = true;
                    NotificationUtil.sendErrorNotification(getProject(), "Error initializing debug environment.", e.getMessage());
                    getSession().stop();
                }
            }
        }.start();
    }

    private void synchronizeSession() {
        final XDebugSession session = getSession();
        final Project project = getProject();
        new BackgroundTask(project, "Initialize debug environment", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                final DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();

                if (status.PROCESS_IS_TERMINATING || status.TARGET_EXECUTION_TERMINATED) {
                    session.stop();
                } else {
                    BackgroundTask sessionSynchronizeTask = new BackgroundTask(project, "Synchronizing debug session", false) {
                        @Override
                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                            try {
                                executeTarget();
                                Thread.sleep(1000);
                                if (!status.TARGET_EXECUTION_THREW_EXCEPTION && !status.TARGET_EXECUTION_TERMINATED) {
                                    runtimeInfo = debuggerInterface.synchronizeSession(debugConnection);
                                    runtimeInfo = debuggerInterface.stepOver(debugConnection);
                                    progressIndicator.setText("Suspending session");
                                    console.system("Debug session synchronized");
                                    suspendSession();
                                }

                            } catch (SQLException e) {
                                status.SESSION_INITIALIZATION_THREW_EXCEPTION = true;
                                console.system("Error synchronizing debug session: " + e.getMessage());
                                MessageUtil.showErrorDialog(project, "Could not initialize debug environment on connection \"" + getConnectionHandler().getName() + "\". ", e);
                                session.stop();
                            }

                        }
                    };


                    status.CAN_SET_BREAKPOINTS = true;
                    progressIndicator.setText("Registering breakpoints");
                    registerBreakpoints(sessionSynchronizeTask);
                }
            }
        }.start();
    }

    private void executeTarget() {
        new DBDebugOperationTask(getProject(), "execute target program") {
            public void execute() throws SQLException {

                if (status.PROCESS_IS_TERMINATING) return;
                if (status.SESSION_INITIALIZATION_THREW_EXCEPTION) return;
                try {
                    status.TARGET_EXECUTION_STARTED = true;
                    console.system("Target program execution started: " + getExecutionInput().getExecutionContext().getTargetName());
                    doExecuteTarget();
                    console.system("Target program execution ended");
                } catch (SQLException e){
                    status.TARGET_EXECUTION_THREW_EXCEPTION = true;
                    console.error("Target program execution failed. " + e.getMessage());
                    // if the method execution threw exception, the debugger-off statement is not reached,
                    // hence the session will hag as debuggable. To avoid this, disable debugging has
                    // to explicitly be called here

                    // TODO: is this required? the target connection will be dropped anyways
                    //DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    //debuggerInterface.disableDebugging(targetConnection);

                    MessageUtil.showErrorDialog(getProject(), "Error executing " + executionInput.getExecutionContext().getTargetName(), e);
                } finally {
                    status.TARGET_EXECUTION_TERMINATED = true;
                    getSession().stop();
                }
            }
        }.start();
    }

    protected abstract void doExecuteTarget() throws SQLException;

    /**
     * breakpoints need to be registered after the database session is started,
     * otherwise they do not get valid ids
     */
    private void registerBreakpoints(final RunnableTask callback) {
        console.system("Registering breakpoints");
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
                console.system("Done registering breakpoints");
                callback.start();
            }
        }.start();
    }

    protected void registerDefaultBreakpoint() {}

    /**
     * breakpoints need to be unregistered before closing the database session, otherwise they remain resident.
     */
    private void unregisterBreakpoints() {
        final Collection<XLineBreakpoint> breakpoints = new ReadActionRunner<Collection<XLineBreakpoint>>() {
            @Override
            protected Collection<XLineBreakpoint> run() {
                XBreakpointType localXBreakpointType = XDebuggerUtil.getInstance().findBreakpointType(breakpointHandler.getBreakpointTypeClass());
                Project project = getProject();
                XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
                return breakpointManager.getBreakpoints(localXBreakpointType);
            }
        }.start();

        Set<Integer> unregisteredBreakpointIds = new HashSet<Integer>();
        for (XLineBreakpoint breakpoint : breakpoints) {
            Integer breakpointId = breakpoint.getUserData(DBJdbcBreakpointHandler.BREAKPOINT_ID_KEY);
            if (breakpointId != null) {
                if (!unregisteredBreakpointIds.contains(breakpointId)) {
                    breakpointHandler.unregisterBreakpoint(breakpoint, false);
                    unregisteredBreakpointIds.add(breakpointId);
                }
                breakpoint.putUserData(DBJdbcBreakpointHandler.BREAKPOINT_ID_KEY, null);
            }

        }

        try {
            if (defaultBreakpointInfo != null && defaultBreakpointInfo.getBreakpointId() != null) {
                getDebuggerInterface().removeBreakpoint(defaultBreakpointInfo.getBreakpointId(), debugConnection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void stop() {
        if (!status.PROCESS_IS_TERMINATED && !status.PROCESS_IS_TERMINATING) {
            console.system("Stopping debugger...");
            status.PROCESS_IS_TERMINATING = true;
            executionInput.getExecutionContext().setExecutionCancelled(!status.PROCESS_STOPPED_NORMALLY);
            stopDebugger();
        }
    }

    private void stopDebugger() {
        final Project project = getProject();
        new BackgroundTask(project, "Stopping debugger", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setText("Stopping debug environment.");
                ConnectionHandler connectionHandler = getConnectionHandler();
                try {
                    unregisterBreakpoints();
                    status.CAN_SET_BREAKPOINTS = false;
                    rollOutDebugger();

                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    if (!status.TARGET_EXECUTION_TERMINATED) {
                        runtimeInfo = debuggerInterface.stopExecution(debugConnection);
                    }
                    debuggerInterface.detachSession(debugConnection);
                    console.system("Debugger session detached");
                } catch (final SQLException e) {
                    console.error("Error detaching debugger session: " + e.getMessage());
                } finally {
                    status.PROCESS_IS_TERMINATED = true;
                    releaseDebugConnection();
                    releaseTargetConnection();
                    DatabaseDebuggerManager.getInstance(project).unregisterDebugSession(connectionHandler);
                    console.system("Debugger stopped");
                }
            }
        }.start();
    }

    private void releaseDebugConnection() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.dropPoolConnection(debugConnection);
        debugConnection = null;
    }

    protected void releaseTargetConnection() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.dropPoolConnection(targetConnection);
        targetConnection = null;
    }

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
                            defaultBreakpointInfo = getDebuggerInterface().addProgramBreakpoint(
                                    method.getSchema().getName(),
                                    schemaObject.getName(),
                                    schemaObject.getObjectType().getName().toUpperCase(),
                                    line,
                                    getDebugConnection());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    @Override
    public void startStepOver() {
        new DBDebugOperationTask(getProject(), "step over") {
            public void execute() throws Exception {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepOver(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void startStepInto() {
        new DBDebugOperationTask(getProject(), "step into") {
            public void execute() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepInto(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void startStepOut() {
        new DBDebugOperationTask(getProject(), "step out") {
            public void execute() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepOut(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void resume() {
        new DBDebugOperationTask(getProject(), "resume execution") {
            public void execute() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.resumeExecution(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void runToPosition(@NotNull final XSourcePosition position) {
        new DBDebugOperationTask(getProject(), "run to position") {
            public void execute() throws SQLException {
                DBSchemaObject object = DBDebugUtil.getObject(position);
                if (object != null) {
                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    runtimeInfo = debuggerInterface.runToPosition(
                            object.getSchema().getName(),
                            object.getName(),
                            object.getObjectType().getName().toUpperCase(),
                            position.getLine(),
                            debugConnection);
                }

                suspendSession();
            }
        }.start();
    }

    @NotNull
    @Override
    public XDebugTabLayouter createTabLayouter() {
        return new DBDebugTabLayouter();
    }

    @Override
    public void startPausing() {
        // NOT SUPPORTED!!!
        new DBDebugOperationTask(getProject(), "run to position") {
            public void execute() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.synchronizeSession(debugConnection);
                suspendSession();
            }
        }.start();
    }

    private void showErrorDialog(SQLException e) {
        MessageUtil.showErrorDialog(getProject(), "Could not perform operation.", e);
    }

    private void suspendSession() {
        if (status.PROCESS_IS_TERMINATING) return;
        Project project = getProject();
        if (isTerminated()) {
            int reasonCode = runtimeInfo.getReason();
            String message = "Session terminated with code :" + reasonCode + " (" + getDebuggerInterface().getRuntimeEventReason(reasonCode) + ")";
            NotificationUtil.sendInfoNotification(project, Constants.DBN_TITLE_PREFIX + "Debugger", message);

            status.PROCESS_STOPPED_NORMALLY = true;
            getSession().stop();
        } else {
            try {
                backtraceInfo = getDebuggerInterface().getExecutionBacktraceInfo(debugConnection);
                List<DebuggerRuntimeInfo> frames = backtraceInfo.getFrames();
                if (frames.size() > 0) {
                    DebuggerRuntimeInfo topRuntimeInfo = frames.get(0);
                    if (runtimeInfo.isTerminated()) {
                        int reasonCode = runtimeInfo.getReason();
                        String message = "Session terminated with code :" + reasonCode + " (" + getDebuggerInterface().getRuntimeEventReason(reasonCode) + ")";
                        NotificationUtil.sendInfoNotification(project, Constants.DBN_TITLE_PREFIX + "Debugger", message);
                    }
                    if (!runtimeInfo.equals(topRuntimeInfo)) {
                        runtimeInfo = topRuntimeInfo;
                        resume();
                        return;
                    }
                }
            } catch (SQLException e) {
                NotificationUtil.sendErrorNotification(project, "Error suspending debugger session.", e.getMessage());
                //showErrorDialog(e);
            }
            VirtualFile virtualFile = getRuntimeInfoFile(runtimeInfo);
            DBProgramDebugSuspendContext suspendContext = new DBProgramDebugSuspendContext(this);
            getSession().positionReached(suspendContext);
            navigateInEditor(virtualFile, runtimeInfo.getLineNumber());
        }
    }

    protected boolean isTerminated() {
        return runtimeInfo.isTerminated();
    }

    @Nullable
    public VirtualFile getRuntimeInfoFile(DebuggerRuntimeInfo runtimeInfo) {
        DBSchemaObject schemaObject = getDatabaseObject(runtimeInfo);
        if (schemaObject != null) {
            return schemaObject.getVirtualFile();
        }
        return null;
    }

    @Nullable
    public DBSchemaObject getDatabaseObject(DebuggerRuntimeInfo runtimeInfo) {
        String ownerName = runtimeInfo.getOwnerName();
        String programName = runtimeInfo.getProgramName();

        if (StringUtil.isNotEmpty(ownerName) && StringUtil.isNotEmpty(programName)) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            DBSchema schema = FailsafeUtil.get(objectBundle.getSchema(ownerName));
            DBSchemaObject schemaObject = schema.getProgram(programName);
            if (schemaObject == null) schemaObject = schema.getMethod(programName, 0); // overload 0 is assuming debug is only supported in oracle (no schema method overloading)
            return schemaObject;
        }
        return null;
    }

    private void rollOutDebugger() {
        try {
            long millis = System.currentTimeMillis();
            while (!status.TARGET_EXECUTION_THREW_EXCEPTION && runtimeInfo != null && !runtimeInfo.isTerminated()) {
                runtimeInfo = getDebuggerInterface().stepOut(debugConnection);
                // force closing the target connection
                if (System.currentTimeMillis() - millis > 20000) {
                    break;
                }
            }
        } catch (SQLException e) {
            NotificationUtil.sendErrorNotification(getProject(), "Error stopping debugger session.", e.getMessage());
            //showErrorDialog(e);
        }
    }

    private void navigateInEditor(final VirtualFile virtualFile, final int line) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                Project project = getProject();
                LogicalPosition position = new LogicalPosition(line, 0);
                if (virtualFile instanceof DBEditableObjectVirtualFile) {
                    DBEditableObjectVirtualFile objectVirtualFile = (DBEditableObjectVirtualFile) virtualFile;
                    // todo review this
                    SourceCodeEditor sourceCodeEditor = null;
                    DBSourceCodeVirtualFile mainContentFile = (DBSourceCodeVirtualFile) objectVirtualFile.getMainContentFile();
                    if (objectVirtualFile.getContentFiles().size() > 1) {
                        FileEditorManager editorManager = FileEditorManager.getInstance(project);
                        FileEditor[] fileEditors = editorManager.getEditors(objectVirtualFile);
                        if (fileEditors.length >= runtimeInfo.getNamespace()) {
                            FileEditor fileEditor = fileEditors[runtimeInfo.getNamespace() -1];
                            sourceCodeEditor = (SourceCodeEditor) fileEditor;
                            objectVirtualFile.FAKE_DOCUMENT.set(sourceCodeEditor.getEditor().getDocument());
                        } else {
                            FileEditor fileEditor = EditorUtil.getTextEditor(objectVirtualFile, mainContentFile);
                            if (fileEditor != null && fileEditor instanceof SourceCodeEditor) {
                                sourceCodeEditor = (SourceCodeEditor) fileEditor;
                            }
                        }
                    } else {
                        FileEditor fileEditor = EditorUtil.getTextEditor(objectVirtualFile, mainContentFile);
                        if (fileEditor != null && fileEditor instanceof SourceCodeEditor) {
                            sourceCodeEditor = (SourceCodeEditor) fileEditor;
                        }
                    }
                    if (sourceCodeEditor != null) {
                        EditorUtil.selectEditor(project, sourceCodeEditor, objectVirtualFile, sourceCodeEditor.getEditorProviderId(), true);
                        sourceCodeEditor.getEditor().getScrollingModel().scrollTo(position, ScrollType.CENTER);
                    }
                }
                else if (virtualFile instanceof DBVirtualFile){
                    FileEditorManager editorManager = FileEditorManager.getInstance(project);
                    FileEditor[] fileEditors = editorManager.openFile(virtualFile, true);
                    for (FileEditor fileEditor : fileEditors) {
                        if (fileEditor instanceof BasicTextEditor) {
                            BasicTextEditor textEditor = (BasicTextEditor) fileEditor;
                            textEditor.getEditor().getScrollingModel().scrollTo(position, ScrollType.CENTER);
                            break;
                        }
                    }
                }

            }
        }.start();
    }

    public DatabaseDebuggerInterface getDebuggerInterface() {
        return getConnectionHandler().getInterfaceProvider().getDebuggerInterface();
    }

    public DebuggerRuntimeInfo getRuntimeInfo() {
        return runtimeInfo;
    }

    public ExecutionBacktraceInfo getBacktraceInfo() {
        return backtraceInfo;
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Database Debug Process";
    }

}
