package com.dci.intellij.dbn.debugger.common.breakpoint;

import java.sql.SQLException;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.*;

public abstract class DBBreakpointHandler<T extends DBDebugProcess> extends XBreakpointHandler<XLineBreakpoint<DBBreakpointProperties>> {
    private XDebugSession session;
    private T debugProcess;

    protected DBBreakpointHandler(XDebugSession session, T debugProcess) {
        super(DBBreakpointType.class);
        this.session = session;
        this.debugProcess = debugProcess;
    }

    public XDebugSession getSession() {
        return session;
    }

    public T getDebugProcess() {
        return debugProcess;
    }


    public void registerBreakpoints(Collection<XLineBreakpoint<DBBreakpointProperties>> breakpoints) {
        for (XLineBreakpoint<DBBreakpointProperties> breakpoint : breakpoints) {
            initializeResources(breakpoint);
        }
        try { Thread.sleep(2000); } catch (InterruptedException ignore) {}

        for (XLineBreakpoint<DBBreakpointProperties> breakpoint : breakpoints) {
            registerBreakpoint(breakpoint);
        }
    }

    public void initializeResources(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        // no initialization by default
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        DBDebugProcess debugProcess = getDebugProcess();
        DBDebugConsoleLogger console = debugProcess.getConsole();

        XDebugSession session = getSession();
        if (!debugProcess.getStatus().CAN_SET_BREAKPOINTS) return;

        ConnectionHandler connectionHandler = debugProcess.getConnectionHandler();
        VirtualFile virtualFile = getVirtualFile(breakpoint);
        Project project = session.getProject();
        if (virtualFile == null) {
            XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
            debuggerManager.getBreakpointManager().removeBreakpoint(breakpoint);
        } else {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler breakpointConnectionHandler = connectionMappingManager.getActiveConnection(virtualFile);

            if (breakpointConnectionHandler == connectionHandler) {
                String breakpointDesc = DBBreakpointUtil.getBreakpointDesc(breakpoint);

                try {
                    if (getBreakpointId(breakpoint) != null) {
                        enableBreakpoint(breakpoint);

                    } else {
                        BreakpointInfo breakpointInfo = addBreakpoint(breakpoint);
                        String error = breakpointInfo.getError();
                        if (error != null) {
                            session.updateBreakpointPresentation( breakpoint,
                                    Icons.DEBUG_INVALID_BREAKPOINT,
                                    "INVALID: " + error);
                            console.error("Error adding breakpoint: " + breakpointDesc + ". " + error);
                        } else {
                            Integer breakpointId = breakpointInfo.getBreakpointId();
                            setBreakpointId(breakpoint, breakpointId);

                            if (!breakpoint.isEnabled()) {
                                error = disableBreakpoint(breakpointId);
                                if (error != null) {
                                    session.updateBreakpointPresentation( breakpoint,
                                            Icons.DEBUG_INVALID_BREAKPOINT,
                                            "INVALID: " + error);
                                }

                            }
                            console.system("Breakpoint added: " + breakpointDesc);
                        }
                    }

                } catch (SQLException e) {
                    console.error("Error adding breakpoint: " + breakpointDesc + ". " + e.getMessage());
                    session.updateBreakpointPresentation( breakpoint,
                            Icons.DEBUG_INVALID_BREAKPOINT,
                            "INVALID: " + e.getMessage());
                } catch (Exception e) {
                    console.error("Error adding breakpoint: " + breakpointDesc + ". " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint, boolean temporary) {
        DBDebugProcess debugProcess = getDebugProcess();

        if (!debugProcess.getStatus().CAN_SET_BREAKPOINTS) return;

        Integer breakpointId = getBreakpointId(breakpoint);
        if (breakpointId != null) {
            DBDebugConsoleLogger console = debugProcess.getConsole();

            VirtualFile virtualFile = getVirtualFile(breakpoint);
            if (virtualFile != null) {
                Project project = session.getProject();
                FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
                ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);
                if (connectionHandler != null && connectionHandler == debugProcess.getConnectionHandler()) {
                    String breakpointDesc = getBreakpointDesc(breakpoint);
                    try {
                        removeBreakpoint(temporary, breakpointId);
                        console.system("Breakpoint removed: " + breakpointDesc);
                    } catch (SQLException e) {
                        console.error("Error removing breakpoint: " + breakpointDesc + ". " + e.getMessage());
                        NotificationUtil.sendErrorNotification(project, "Error.", e.getMessage());
                    } finally {
                        setBreakpointId(breakpoint, null);
                    }
                }
            }
        }

    }

    protected abstract BreakpointInfo addBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) throws Exception;

    protected DatabaseDebuggerInterface getDebuggerInterface(@NotNull DBSchemaObject object) {
        return object.getConnectionHandler().getInterfaceProvider().getDebuggerInterface();
    }

    protected abstract void removeBreakpoint(boolean temporary, Integer breakpointId) throws SQLException;

    protected abstract void enableBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) throws Exception;


    protected abstract String disableBreakpoint(Integer breakpointId) throws SQLException;

    private void resetBreakpoints() {
        Project project = session.getProject();

        XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
        XBreakpoint<?>[] breakpoints = breakpointManager.getAllBreakpoints();

        for (XBreakpoint breakpoint : breakpoints) {
            if (breakpoint.getType() instanceof DBBreakpointType) {
                XLineBreakpoint lineBreakpoint = (XLineBreakpoint) breakpoint;
                VirtualFile virtualFile = getVirtualFile(lineBreakpoint);
                if (virtualFile != null) {
                    FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
                    ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);

                    if (connectionHandler == debugProcess.getConnectionHandler()) {
                        setBreakpointId(lineBreakpoint, null);
                    }
                }
            }
        }
    }
}
