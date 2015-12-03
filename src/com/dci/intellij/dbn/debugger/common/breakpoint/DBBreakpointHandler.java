package com.dci.intellij.dbn.debugger.common.breakpoint;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.getBreakpointDesc;

public abstract class DBBreakpointHandler<T extends DBDebugProcess> extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {
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


    protected boolean canSetBreakpoints() {
        return getDebugProcess().getStatus().CAN_SET_BREAKPOINTS;
    }

    @Override
    public final void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
        if (canSetBreakpoints()) {
            XBreakpointProperties properties = breakpoint.getProperties();
            if (properties instanceof DBBreakpointProperties) {
                DBBreakpointProperties breakpointProperties = (DBBreakpointProperties) properties;
                if (getConnectionHandler() == breakpointProperties.getConnectionHandler()) {
                    registerDatabaseBreakpoint(breakpoint);
                }
            }
        }
    }

    @Override
    public final void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, boolean temporary) {
        XBreakpointProperties properties = breakpoint.getProperties();
        if (properties instanceof DBBreakpointProperties) {
            DBBreakpointProperties breakpointProperties = (DBBreakpointProperties) properties;
            if (getConnectionHandler() == breakpointProperties.getConnectionHandler()) {
                unregisterDatabaseBreakpoint(breakpoint, temporary);
            }
        }
    }

    protected abstract void registerDatabaseBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint);

    protected abstract void unregisterDatabaseBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, boolean temporary);

    public void registerBreakpoints(Collection<XLineBreakpoint<XBreakpointProperties>> breakpoints) {
        for (XLineBreakpoint<XBreakpointProperties> breakpoint : breakpoints) {
            registerBreakpoint(breakpoint);
        }
    }

    protected void handleBreakpointError(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, String error) {
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

    protected ConnectionHandler getConnectionHandler() {
        return getDebugProcess().getConnectionHandler();
    }

    protected DatabaseDebuggerInterface getDebuggerInterface() {
        return getConnectionHandler().getInterfaceProvider().getDebuggerInterface();
    }

    public abstract void registerDefaultBreakpoint(DBMethod method);

    public abstract void unregisterDefaultBreakpoint();

    public abstract void prepareObjectClasses(Collection<XLineBreakpoint<XBreakpointProperties>> breakpoints);
}
