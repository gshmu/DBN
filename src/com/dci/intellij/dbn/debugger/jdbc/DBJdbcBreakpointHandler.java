package com.dci.intellij.dbn.debugger.jdbc;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.database.common.debug.BreakpointOperationInfo;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class DBJdbcBreakpointHandler extends DBBreakpointHandler<DBJdbcDebugProcess> {

    public DBJdbcBreakpointHandler(XDebugSession session, DBJdbcDebugProcess debugProcess) {
        super(session, debugProcess);
        //resetBreakpoints();
    }

    protected BreakpointInfo addBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint, DBSchemaObject object) throws Exception {
        DBJdbcDebugProcess debugProcess = getDebugProcess();
        ConnectionHandler connectionHandler = debugProcess.getConnectionHandler();
        DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
        Connection debugConnection = debugProcess.getDebugConnection();
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

    @Override
    protected void removeBreakpoint(boolean temporary, Integer breakpointId) throws SQLException {
        DBJdbcDebugProcess debugProcess = getDebugProcess();
        ConnectionHandler connectionHandler = debugProcess.getConnectionHandler();
        Connection debugConnection = debugProcess.getDebugConnection();
        DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
        if (temporary) {
            debuggerInterface.disableBreakpoint(breakpointId, debugConnection);
        } else {
            debuggerInterface.removeBreakpoint(breakpointId, debugConnection);
        }
    }

    @Override
    protected void enableBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) throws Exception {
        Integer breakpointId = getBreakpointId(breakpoint);
        if (breakpointId != null) {
            DBJdbcDebugProcess debugProcess = getDebugProcess();
            ConnectionHandler connectionHandler = debugProcess.getConnectionHandler();
            Connection debugConnection = debugProcess.getDebugConnection();

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

    @Override
    protected String disableBreakpoint(Integer breakpointId) throws SQLException {
        DBJdbcDebugProcess debugProcess = getDebugProcess();
        ConnectionHandler connectionHandler = debugProcess.getConnectionHandler();
        DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
        Connection debugConnection = debugProcess.getDebugConnection();
        BreakpointOperationInfo breakpointOperationInfo = debuggerInterface.disableBreakpoint(breakpointId, debugConnection);
        return breakpointOperationInfo.getError();
    }



}
