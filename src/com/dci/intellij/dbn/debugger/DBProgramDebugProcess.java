package com.dci.intellij.dbn.debugger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.Presentable;
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
import com.dci.intellij.dbn.debugger.breakpoint.DBProgramBreakpointHandler;
import com.dci.intellij.dbn.debugger.evaluation.DBProgramDebuggerEditorsProvider;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.frame.DBProgramDebugSuspendContext;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
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

public abstract class DBProgramDebugProcess<T extends ExecutionInput> extends XDebugProcess implements Presentable{
    private Connection targetConnection;
    private Connection debugConnection;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBProgramBreakpointHandler breakpointHandler;
    private DBProgramBreakpointHandler[] breakpointHandlers;
    private T executionInput;
    protected BreakpointInfo defaultBreakpointInfo;
    private DBProgramDebugProcessStatus status = new DBProgramDebugProcessStatus();

    private transient DebuggerRuntimeInfo runtimeInfo;
    private transient ExecutionBacktraceInfo backtraceInfo;


    public DBProgramDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        Project project = session.getProject();
        DatabaseDebuggerManager.getInstance(project).registerDebugSession(connectionHandler);

        DBProgramRunConfiguration<T> runProfile = (DBProgramRunConfiguration) session.getRunProfile();

        executionInput = runProfile.getExecutionInput();

        breakpointHandler = new DBProgramBreakpointHandler(session, this);
        breakpointHandlers = new DBProgramBreakpointHandler[]{breakpointHandler};
    }

    public DBProgramDebugProcessStatus getStatus() {
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
    public void sessionInitialized() {
        final Project project = getProject();
        new BackgroundTask(project, "Initialize debug environment", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                try {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    targetConnection = connectionHandler.getPoolConnection(executionInput.getExecutionContext().getTargetSchema());
                    targetConnection.setAutoCommit(false);
                    debugConnection = connectionHandler.getPoolConnection();

                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    progressIndicator.setText("Initializing debugger target session");
                    DebuggerSessionInfo sessionInfo = debuggerInterface.initializeSession(targetConnection);
                    debuggerInterface.enableDebugging(targetConnection);
                    debuggerInterface.attachSession(sessionInfo.getSessionId(), debugConnection);

                    synchronizeSession();
                } catch (SQLException e) {
                    getSession().stop();
                    showErrorDialog(e);
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
                                runtimeInfo = debuggerInterface.synchronizeSession(debugConnection);

                                if (status.TARGET_EXECUTION_TERMINATED) {
                                    session.stop();
                                } else {
                                    runtimeInfo = debuggerInterface.stepOver(debugConnection);
                                    progressIndicator.setText("Suspending session");
                                    suspendSession();
                                }
                            } catch (SQLException e) {
                                status.SESSION_SYNCHRONIZING_THREW_EXCEPTION = true;
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
        final Project project = getProject();
        new DebugOperationThread("execute method", project) {
            public void executeOperation() throws SQLException {

                if (status.PROCESS_IS_TERMINATING) return;
                if (status.SESSION_SYNCHRONIZING_THREW_EXCEPTION) return;
                try {
                    status.TARGET_EXECUTION_STARTED = true;
                    doExecuteTarget();

                } catch (SQLException e){
                    // if the method execution threw exception, the debugger-off statement is not reached,
                    // hence the session will hag as debuggable. To avoid this, disable debugging has
                    // to explicitly be called here
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

    /**
     * breakpoints need to be registered after the database session is started,
     * otherwise they do not get valid ids
     */
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

        for (XLineBreakpoint breakpoint : breakpoints) {
            breakpointHandler.unregisterBreakpoint(breakpoint, false);
        }

        try {
            if (defaultBreakpointInfo != null && debugConnection != null) {
                getDebuggerInterface().removeBreakpoint(defaultBreakpointInfo.getBreakpointId(), debugConnection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void stop() {
        if (!status.PROCESS_IS_TERMINATED && !status.PROCESS_IS_TERMINATING) {
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
                } catch (final SQLException e) {
                    showErrorDialog(e);
                } finally {
                    status.PROCESS_IS_TERMINATED = true;
                    releaseTargetConnection();
                    connectionHandler.dropPoolConnection(debugConnection);
                    debugConnection = null;
                    DatabaseDebuggerManager.getInstance(project).unregisterDebugSession(connectionHandler);
                }
            }
        }.start();
    }

    private void releaseTargetConnection() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
        try {
            debuggerInterface.disableDebugging(targetConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.dropPoolConnection(targetConnection);
            targetConnection = null;
        }
    }


    @Override
    public void startStepOver() {
        new DebugOperationThread("step over", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepOver(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void startStepInto() {
        new DebugOperationThread("step into", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepInto(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void startStepOut() {
        new DebugOperationThread("step out", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepOut(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void resume() {
        new DebugOperationThread("resume execution", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.resumeExecution(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void runToPosition(@NotNull final XSourcePosition position) {
        new DebugOperationThread("run to position", getProject()) {
            public void executeOperation() throws SQLException {
                DBSchemaObject object = DBProgramDebugUtil.getObject(position);
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
        return new DBProgramDebugTabLayouter();
    }

    @Override
    public void startPausing() {
        // NOT SUPPORTED!!!
        new DebugOperationThread("run to position", getProject()) {
            public void executeOperation() throws SQLException {
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

        if (isTerminated()) {
            status.PROCESS_STOPPED_NORMALLY = true;
            getSession().stop();
        } else {
            try {
                backtraceInfo = getDebuggerInterface().getExecutionBacktraceInfo(debugConnection);
            } catch (SQLException e) {
                showErrorDialog(e);
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
            showErrorDialog(e);
        }
    }

    private void navigateInEditor(final VirtualFile virtualFile, final int line) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                Project project = getProject();
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
                    LogicalPosition position = new LogicalPosition(line, 0);
                    if (sourceCodeEditor != null) {
                        EditorUtil.selectEditor(project, sourceCodeEditor, objectVirtualFile, sourceCodeEditor.getEditorProviderId(), true);
                        sourceCodeEditor.getEditor().getScrollingModel().scrollTo(position, ScrollType.CENTER);
                    }
                }
                else {
                    FileEditorManager editorManager = FileEditorManager.getInstance(project);
                    FileEditor[] fileEditors = editorManager.openFile(virtualFile, true);
                    for (FileEditor fileEditor : fileEditors) {

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

    abstract class DebugOperationThread extends Thread {
        private String operationName;
        protected DebugOperationThread(String operationName, Project project) {
            super("DBN Debug Operation (" + operationName + ')');
            this.operationName = operationName;
        }

        @Override
        public final void run() {
            try {
                executeOperation();
            } catch (final SQLException e) {
                MessageUtil.showErrorDialog(getProject(), "Could not perform debug operation (" + operationName + ").", e);
            }
        }
        public abstract void executeOperation() throws SQLException;
    }
}
