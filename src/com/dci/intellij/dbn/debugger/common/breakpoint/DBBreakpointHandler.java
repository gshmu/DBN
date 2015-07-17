package com.dci.intellij.dbn.debugger.common.breakpoint;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.getBreakpointDesc;

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
            registerBreakpoint(breakpoint);
        }
    }

    protected void handleBreakpointError(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint, String error) {
        DBDebugConsoleLogger console = getConsole();
        XDebugSession session = getSession();
        String breakpointDesc = getBreakpointDesc(breakpoint);
        console.error("Failed to add breakpoint: " + breakpointDesc + " (" + error + ")");
        session.updateBreakpointPresentation( breakpoint,
                Icons.DEBUG_INVALID_BREAKPOINT,
                "INVALID: " + error);
    }

    private DBDebugConsoleLogger getConsole() {
        return getDebugProcess().getConsole();
    }

    protected DatabaseDebuggerInterface getDebuggerInterface(@NotNull DBSchemaObject object) {
        return object.getConnectionHandler().getInterfaceProvider().getDebuggerInterface();
    }

    protected ConnectionHandler getConnectionHandler() {
        return getDebugProcess().getConnectionHandler();
    }
}
