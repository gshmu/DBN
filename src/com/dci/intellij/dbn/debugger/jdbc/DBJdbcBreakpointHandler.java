package com.dci.intellij.dbn.debugger.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.database.common.debug.BreakpointOperationInfo;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointType;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.*;

public class DBJdbcBreakpointHandler extends DBBreakpointHandler<DBJdbcDebugProcess> {

    public DBJdbcBreakpointHandler(XDebugSession session, DBJdbcDebugProcess debugProcess) {
        super(session, debugProcess);
        //resetBreakpoints();
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
                            handleBreakpointError(breakpoint, error);
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

                } catch (Exception e) {
                    handleBreakpointError(breakpoint, e.getMessage());
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
                Project project = getSession().getProject();
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

    private Connection getDebugConnection() {
        DBJdbcDebugProcess debugProcess = getDebugProcess();
        return debugProcess.getDebugConnection();
    }

    private BreakpointInfo addBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) throws Exception {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
        Connection debugConnection = getDebugConnection();
        DBSchemaObject object = getDatabaseObject(breakpoint);
        return object == null ?
                debuggerInterface.addSourceBreakpoint(
                        breakpoint.getLine(),
                        debugConnection) :
                debuggerInterface.addProgramBreakpoint(
                        object.getSchema().getName(),
                        object.getName(),
                        object.getObjectType().getName().toUpperCase(),
                        breakpoint.getLine(),
                        debugConnection);
    }

    private void removeBreakpoint(boolean temporary, Integer breakpointId) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Connection debugConnection = getDebugConnection();
        DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
        if (temporary) {
            debuggerInterface.disableBreakpoint(breakpointId, debugConnection);
        } else {
            debuggerInterface.removeBreakpoint(breakpointId, debugConnection);
        }
    }

    private void enableBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) throws Exception {
        Integer breakpointId = getBreakpointId(breakpoint);
        if (breakpointId != null) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            Connection debugConnection = getDebugConnection();

            DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
            BreakpointOperationInfo breakpointOperationInfo = debuggerInterface.enableBreakpoint(breakpointId, debugConnection);
            String error = breakpointOperationInfo.getError();
            if (error != null) {
                getSession().updateBreakpointPresentation(breakpoint,
                        Icons.DEBUG_INVALID_BREAKPOINT,
                        "INVALID: " + error);
            }

        }
    }

    private String disableBreakpoint(Integer breakpointId) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
        Connection debugConnection = getDebugConnection();
        BreakpointOperationInfo breakpointOperationInfo = debuggerInterface.disableBreakpoint(breakpointId, debugConnection);
        return breakpointOperationInfo.getError();
    }

    private void resetBreakpoints() {
        Project project = getSession().getProject();

        XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
        XBreakpoint<?>[] breakpoints = breakpointManager.getAllBreakpoints();

        for (XBreakpoint breakpoint : breakpoints) {
            if (breakpoint.getType() instanceof DBBreakpointType) {
                XLineBreakpoint lineBreakpoint = (XLineBreakpoint) breakpoint;
                VirtualFile virtualFile = getVirtualFile(lineBreakpoint);
                if (virtualFile != null) {
                    FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
                    ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);

                    if (connectionHandler == getDebugProcess().getConnectionHandler()) {
                        setBreakpointId(lineBreakpoint, null);
                    }
                }
            }
        }
    }



}
