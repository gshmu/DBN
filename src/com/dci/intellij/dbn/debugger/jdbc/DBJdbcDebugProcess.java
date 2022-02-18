package com.dci.intellij.dbn.debugger.jdbc;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Write;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerSessionInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.DBDebugOperation;
import com.dci.intellij.dbn.debugger.DBDebugTabLayouter;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatusHolder;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBJdbcDebuggerEditorsProvider;
import com.dci.intellij.dbn.debugger.jdbc.frame.DBJdbcDebugSuspendContext;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.getBreakpointId;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.setBreakpointId;
import static com.dci.intellij.dbn.debugger.common.process.DBDebugProcessStatus.*;
import static com.dci.intellij.dbn.execution.ExecutionStatus.CANCELLED;

public abstract class DBJdbcDebugProcess<T extends ExecutionInput> extends XDebugProcess implements DBDebugProcess, NotificationSupport {
    protected DBNConnection targetConnection;
    private DBNConnection debugConnection;
    private final DBDebugProcessStatusHolder status = new DBDebugProcessStatusHolder();
    private final ConnectionHandlerRef connectionHandler;
    private final DBBreakpointHandler[] breakpointHandlers;
    private final DBDebugConsoleLogger console;

    private transient DebuggerRuntimeInfo runtimeInfo;
    private transient ExecutionBacktraceInfo backtraceInfo;

    public DBJdbcDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session);
        this.console = new DBDebugConsoleLogger(session);
        this.connectionHandler = ConnectionHandlerRef.of(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager.getInstance(project).registerDebugSession(connectionHandler);

        DBJdbcBreakpointHandler breakpointHandler = new DBJdbcBreakpointHandler(session, this);
        breakpointHandlers = new DBBreakpointHandler[]{breakpointHandler};
    }

    @Override
    public boolean set(DBDebugProcessStatus status, boolean value) {
        return this.status.set(status, value);
    }

    @Override
    public boolean is(DBDebugProcessStatus status) {
        return this.status.is(status);
    }
    public DBNConnection getTargetConnection() {
        return targetConnection;
    }

    public DBNConnection getDebugConnection() {
        return debugConnection;
    }

    @Override
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @Override
    @NotNull
    public Project getProject() {
        return getSession().getProject();
    }

    @NotNull
    @Override
    public DBBreakpointHandler[] getBreakpointHandlers() {
        return breakpointHandlers;
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return DBJdbcDebuggerEditorsProvider.INSTANCE;
    }

    @NotNull
    public T getExecutionInput() {
        DBRunConfig<T> runProfile = (DBRunConfig) getSession().getRunProfile();
        if (runProfile == null) throw AlreadyDisposedException.INSTANCE;
        return runProfile.getExecutionInput();
    }

    @Override
    public DBDebugConsoleLogger getConsole() {
        return console;
    }

    @Override
    public void sessionInitialized() {
        Project project = getProject();
        XDebugSession session = getSession();
        if (session instanceof XDebugSessionImpl) {
            XDebugSessionImpl sessionImpl = (XDebugSessionImpl) session;
            sessionImpl.getSessionData().setBreakpointsMuted(false);
        }
        Progress.background(project, "Initializing debug environment", true,
                (progress) -> {
                    try {
                        T executionInput = getExecutionInput();
                        console.system("Initializing debug environment...");
                        ConnectionHandler connectionHandler = getConnectionHandler();
                        SchemaId schemaId = executionInput.getExecutionContext().getTargetSchema();
                        targetConnection = connectionHandler.getDebugConnection(schemaId);
                        targetConnection.setAutoCommit(false);
                        debugConnection = connectionHandler.getDebuggerConnection();
                        console.system("Debug connections allocated");

                        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                        progress.setText("Initializing debugger target session");

                        DebuggerSessionInfo sessionInfo = debuggerInterface.initializeSession(targetConnection);
                        console.system("Debug target session initialized");
                        debuggerInterface.enableDebugging(targetConnection);
                        debuggerInterface.attachSession(debugConnection, sessionInfo.getSessionId());
                        console.system("Attached debug session");

                        synchronizeSession();
                    } catch (SQLException e) {
                        set(SESSION_INITIALIZATION_THREW_EXCEPTION, true);
                        console.error("Error initializing debug environment\n" + e.getMessage());
                        session.stop();
                    }
                });
    }

    private void synchronizeSession() {
        Project project = getProject();
        Progress.background(project, "Initializing debug environment", false,
                (progress) -> {
                    if (is(PROCESS_TERMINATING) || is(TARGET_EXECUTION_TERMINATED)) {
                        getSession().stop();
                    } else {
                        set(BREAKPOINT_SETTING_ALLOWED, true);
                        progress.setText("Registering breakpoints");
                        registerBreakpoints(
                                () -> Progress.background(project, "Synchronizing debug session", false,
                                        (progress1) -> {
                                            DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                                            try {
                                                startTargetProgram();
                                                if (isNot(TARGET_EXECUTION_THREW_EXCEPTION) && isNot(TARGET_EXECUTION_TERMINATED)) {
                                                    runtimeInfo = debuggerInterface.synchronizeSession(debugConnection);
                                                    runtimeInfo = debuggerInterface.stepOver(debugConnection);
                                                    progress.setText("Suspending session");
                                                    console.system("Debug session synchronized");
                                                    suspendSession();
                                                }

                                            } catch (SQLException e) {
                                                set(SESSION_INITIALIZATION_THREW_EXCEPTION, true);
                                                console.system("Error synchronizing debug session: " + e.getMessage());
                                                Messages.showErrorDialog(getProject(),
                                                        "Could not initialize debug environment on connection \"" +
                                                                getConnectionHandler().getName() + "\". ", e);
                                                getSession().stop();
                                            }
                                        }));
                    }
                });
    }

    private void startTargetProgram() {
        Progress.background(getProject(), "Running debugger target program", false,
                (progress) -> {
                    if (is(PROCESS_TERMINATING)) return;
                    if (is(SESSION_INITIALIZATION_THREW_EXCEPTION)) return;
                    T executionInput = getExecutionInput();
                    try {
                        set(TARGET_EXECUTION_STARTED, true);

                        console.system("Target program execution started: " + executionInput.getExecutionContext().getTargetName());
                        executeTarget();
                        console.system("Target program execution ended");
                    } catch (SQLException e) {
                        set(TARGET_EXECUTION_THREW_EXCEPTION, true);
                        console.error("Target program execution failed. " + e.getMessage());
                        // if the method execution threw exception, the debugger-off statement is not reached,
                        // hence the session will hag as debuggable. To avoid this, disable debugging has
                        // to explicitly be called here

                        // TODO: is this required? the target connection will be dropped anyways
                        //DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                        //debuggerInterface.disableDebugging(targetConnection);

                        Messages.showErrorDialog(getProject(), "Error executing " + executionInput.getExecutionContext().getTargetName(), e);
                    } finally {
                        set(TARGET_EXECUTION_TERMINATED, true);
                        getSession().stop();
                    }
                });
    }

    protected abstract void executeTarget() throws SQLException;

    /**
     * breakpoints need to be registered after the database session is started,
     * otherwise they do not get valid ids
     */
    private void registerBreakpoints(Runnable callback) {
        console.system("Registering breakpoints...");
        List<XLineBreakpoint<XBreakpointProperties>> breakpoints = DBBreakpointUtil.getDatabaseBreakpoints(getConnectionHandler());

        Write.run(() -> {
            getBreakpointHandler().registerBreakpoints(breakpoints, null);
            registerDefaultBreakpoint();
            console.system("Done registering breakpoints");
            callback.run();
        });
    }

    protected void registerDefaultBreakpoint() {}

    /**
     * breakpoints need to be unregistered before closing the database session, otherwise they remain resident.
     */
    private void unregisterBreakpoints() {
        Collection<XLineBreakpoint<XBreakpointProperties>> breakpoints = DBBreakpointUtil.getDatabaseBreakpoints(getConnectionHandler());
        Set<Integer> unregisteredBreakpointIds = new HashSet<>();
        DBBreakpointHandler<?> breakpointHandler = getBreakpointHandler();
        for (XLineBreakpoint breakpoint : breakpoints) {
            Integer breakpointId = getBreakpointId(breakpoint);
            if (breakpointId != null) {
                if (!unregisteredBreakpointIds.contains(breakpointId)) {
                    breakpointHandler.unregisterBreakpoint(breakpoint, false);
                    unregisteredBreakpointIds.add(breakpointId);
                }
                setBreakpointId(breakpoint, null);
            }

        }
        breakpointHandler.unregisterDefaultBreakpoint();
    }

    @Override
    public void stop() {
        if (canStopDebuger()) {
            synchronized (this) {
                if (canStopDebuger()) {
                    set(PROCESS_TERMINATING, true);
                    console.system("Stopping debugger...");
                    T executionInput = getExecutionInput();
                    ExecutionContext executionContext = executionInput.getExecutionContext();
                    executionContext.set(CANCELLED, isNot(PROCESS_STOPPED_NORMALLY));
                    stopDebugger();
                }
            }
        }
    }

    private boolean canStopDebuger() {
        return isNot(PROCESS_TERMINATED) && isNot(PROCESS_TERMINATING);
    }

    private void stopDebugger() {
        Project project = getProject();
        Progress.background(project, "Stopping debugger", false,
                (progress) -> {
                    progress.setText("Stopping debug environment.");
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    try {
                        unregisterBreakpoints();
                        set(BREAKPOINT_SETTING_ALLOWED, false);
                        rollOutDebugger();

                        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                        if (debugConnection != null) {
                            if (isNot(TARGET_EXECUTION_TERMINATED)) {
                                runtimeInfo = debuggerInterface.stopExecution(debugConnection);
                            }
                            debuggerInterface.detachSession(debugConnection);
                        }
                        console.system("Debugger session detached");
                    } catch (SQLException e) {
                        console.error("Error detaching debugger session: " + e.getMessage());
                    } finally {
                        set(PROCESS_TERMINATED, true);
                        releaseDebugConnection();
                        releaseTargetConnection();
                        DBRunConfig<T> runProfile = (DBRunConfig<T>) getSession().getRunProfile();
                        if (runProfile != null && runProfile.getCategory() != DBRunConfigCategory.CUSTOM) {
                            runProfile.setCanRun(false);
                        }
                        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
                        debuggerManager.unregisterDebugSession(connectionHandler);
                        console.system("Debugger stopped");
                    }
            });
    }

    private void releaseDebugConnection() {
        Resources.close(debugConnection);
        debugConnection = null;
    }

    protected void releaseTargetConnection() {
        Resources.close(targetConnection);
        targetConnection = null;
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext suspendContext) {
        DBDebugOperation.run(getProject(), "step over", () -> {
            DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
            runtimeInfo = debuggerInterface.stepOver(debugConnection);
            suspendSession();
        });
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext suspendContext) {
        DBDebugOperation.run(getProject(), "step into", () -> {
            DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
            runtimeInfo = debuggerInterface.stepInto(debugConnection);
            suspendSession();
        });
    }


    @Override
    public void startStepOut(@Nullable XSuspendContext suspendContext) {
        DBDebugOperation.run(getProject(), "step out", () -> {
            DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
            runtimeInfo = debuggerInterface.stepOut(debugConnection);
            suspendSession();
        });
    }

    @Override
    public void resume(@Nullable XSuspendContext suspendContext) {
        DBDebugOperation.run(getProject(), "resume execution", () -> {
            DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
            runtimeInfo = debuggerInterface.resumeExecution(debugConnection);
            suspendSession();
        });
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext suspendContext) {
        DBDebugOperation.run(getProject(), "run to position", () -> {
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
        });
    }

    @NotNull
    @Override
    public XDebugTabLayouter createTabLayouter() {
        return new DBDebugTabLayouter();
    }

    @Override
    public void startPausing() {
        // NOT SUPPORTED!!!
        DBDebugOperation.run(getProject(), "run to position", () -> {
            DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
            runtimeInfo = debuggerInterface.synchronizeSession(debugConnection);
            suspendSession();
        });
    }

    private void showErrorDialog(SQLException e) {
        Messages.showErrorDialog(getProject(), "Could not perform operation.", e);
    }

    private void suspendSession() {
        if (is(PROCESS_TERMINATING)) return;
        XDebugSession session = getSession();
        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
        if (isTerminated()) {
            int reasonCode = runtimeInfo.getReason();
            String reason = debuggerInterface.getRuntimeEventReason(reasonCode);
            sendInfoNotification(
                    NotificationGroup.DEBUGGER,
                    "Session terminated with code {0} ({1})", reasonCode, reason);

            set(PROCESS_STOPPED_NORMALLY, true);
            session.stop();
        } else {
            VirtualFile virtualFile = getRuntimeInfoFile(runtimeInfo);
            DBDebugUtil.openEditor(virtualFile);
            try {
                backtraceInfo = debuggerInterface.getExecutionBacktraceInfo(debugConnection);
                List<DebuggerRuntimeInfo> frames = backtraceInfo.getFrames();
                if (frames.size() > 0) {
                    DebuggerRuntimeInfo topRuntimeInfo = frames.get(0);
                    if (runtimeInfo.isTerminated()) {
                        int reasonCode = runtimeInfo.getReason();
                        String reason = debuggerInterface.getRuntimeEventReason(reasonCode);
                        sendInfoNotification(
                                NotificationGroup.DEBUGGER,
                                "Session terminated with code {0} ({1})", reasonCode, reason);
                    }
                    if (!runtimeInfo.equals(topRuntimeInfo)) {
                        runtimeInfo = topRuntimeInfo;
                        resume();
                        return;
                    }
                }
            } catch (SQLException e) {
                console.error("Error suspending debugger session: " + e.getMessage());
            }

            DBJdbcDebugSuspendContext suspendContext = new DBJdbcDebugSuspendContext(this);
            session.positionReached(suspendContext);
            //navigateInEditor(virtualFile, runtimeInfo.getLineNumber());
        }
    }

    protected boolean isTerminated() {
        return runtimeInfo.isTerminated();
    }

    protected DBBreakpointHandler getBreakpointHandler() {
        return getBreakpointHandlers()[0];
    }

    @Nullable
    public VirtualFile getRuntimeInfoFile(DebuggerRuntimeInfo runtimeInfo) {
        DBSchemaObject schemaObject = getDatabaseObject(runtimeInfo);
        if (schemaObject != null) {
            DBObjectVirtualFile virtualFile = schemaObject.getVirtualFile();
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                DBContentType contentType = schemaObject.getContentType();
                if (contentType == DBContentType.CODE_SPEC_AND_BODY) {
                    return editableObjectFile.getContentFile(DBContentType.CODE_BODY);
                } else if (contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_AND_DATA)) {
                    return editableObjectFile.getContentFile(DBContentType.CODE);
                }

            }
        }
        return null;
    }

    @Nullable
    protected DBSchemaObject getDatabaseObject(DebuggerRuntimeInfo runtimeInfo) {
        String ownerName = runtimeInfo.getOwnerName();
        String programName = runtimeInfo.getProgramName();

        if (Strings.isNotEmpty(ownerName) && Strings.isNotEmpty(programName)) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            DBSchema schema = Failsafe.nn(objectBundle.getSchema(ownerName));
            DBSchemaObject schemaObject = schema.getProgram(programName);
            if (schemaObject == null) schemaObject = schema.getMethod(programName, (short) 0); // overload 0 is assuming debug is only supported in oracle (no schema method overloading)
            return schemaObject;
        }
        return null;
    }

    private void rollOutDebugger() {
        try {
            long millis = System.currentTimeMillis();
            while (isNot(TARGET_EXECUTION_THREW_EXCEPTION) && runtimeInfo != null && !runtimeInfo.isTerminated()) {
                runtimeInfo = getDebuggerInterface().stepOut(debugConnection);
                // force closing the target connection
                if (System.currentTimeMillis() - millis > 20000) {
                    break;
                }
            }
        } catch (SQLException e) {
            console.error("Error stopping debugger session: " + e.getMessage());
        }
    }

/*    private void navigateInEditor(final VirtualFile virtualFile, final int line) {
        SimpleLaterInvocator.invoke(() -> {
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
                        FileEditor fileEditor = EditorUtil.getTextEditor(mainContentFile);
                        if (fileEditor instanceof SourceCodeEditor) {
                            sourceCodeEditor = (SourceCodeEditor) fileEditor;
                        }
                    }
                } else {
                    FileEditor fileEditor = EditorUtil.getTextEditor(mainContentFile);
                    if (fileEditor instanceof SourceCodeEditor) {
                        sourceCodeEditor = (SourceCodeEditor) fileEditor;
                    }
                }
                if (sourceCodeEditor != null) {
                    EditorUtil.selectEditor(project, sourceCodeEditor, objectVirtualFile, sourceCodeEditor.getEditorProviderId(), NavigationInstruction.FOCUS_SCROLL);
                    sourceCodeEditor.getEditor().getScrollingModel().scrollTo(position, ScrollType.CENTER);
                }
            } else if (virtualFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
                DBEditableObjectVirtualFile objectVirtualFile = sourceCodeFile.getMainDatabaseFile();
                FileEditorManager editorManager = FileEditorManager.getInstance(project);
                FileEditor[] fileEditors = editorManager.getEditors(objectVirtualFile);
                for (FileEditor fileEditor : fileEditors) {
                    VirtualFile editorFile = fileEditor.getFile();
                    if (editorFile != null && editorFile.equals(sourceCodeFile)) {
                        System.out.println();
                        break;
                    }
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
        });
    }*/

    @Override public void startStepOver() {startStepOver(null);}
    @Override public void startStepInto() {startStepInto(null);}
    @Override public void startStepOut() {startStepOut(null);}
    @Override public void resume() {resume(null);}
    @Override public void runToPosition(@NotNull XSourcePosition position) {runToPosition(position, null);}

    @Override
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
