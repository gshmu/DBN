package com.dci.intellij.dbn.debugger.jdwp;

import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.engine.requests.RequestManagerImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.*;

public class DBJdwpBreakpointHandler extends DBBreakpointHandler<DBJdwpDebugProcess> {
    public DBJdwpBreakpointHandler(XDebugSession session, DBJdwpDebugProcess debugProcess) {
        super(session, debugProcess);
        //resetBreakpoints();
    }

    @Override
    public void initializeResources(@NotNull final XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        DBSchemaObject object = getDatabaseObject(breakpoint);
        if (object != null) {
            getManagerThread().invokeAndWait(new DebuggerCommandImpl() {
                @Override
                protected void action() throws Exception {
                    DBJdwpDebugProcess debugProcess = getDebugProcess();
                    DebugProcessImpl jdiDebugProcess = getJdiDebugProcess();

                    Project project = getSession().getProject();
                    VirtualMachineProxyImpl virtualMachineProxy = jdiDebugProcess.getVirtualMachineProxy();
                    RequestManagerImpl requestsManager = jdiDebugProcess.getRequestsManager();

                    String programIdentifier = getProgramIdentifier(breakpoint);
                    boolean initRequested = debugProcess.isInitRequested(programIdentifier);
                    List<ReferenceType> referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                    if (!initRequested && referenceTypes.size() == 0) {
                        debugProcess.registerInitRequest(programIdentifier);
                        //ClassPrepareRequest classPrepareRequest = eventRequestManager.createClassPrepareRequest();
                        //classPrepareRequest.addClassFilter(programIdentifier);
                        //classPrepareRequest.enable();

                        LineBreakpoint lineBreakpoint = getLineBreakpoint(project, breakpoint);
                        ClassPrepareRequest classPrepareRequest = requestsManager.createClassPrepareRequest(lineBreakpoint, programIdentifier);
                        requestsManager.enableRequest(classPrepareRequest);
                    }
                }
            });

        }
    }

    DebuggerManagerThreadImpl getManagerThread() {
        return getJdiDebugProcess().getManagerThread();
    }

    private DebugProcessImpl getJdiDebugProcess() {
        return getDebugProcess().getDebuggerSession().getProcess();
    }

    @Override
    protected BreakpointInfo addBreakpoint(@NotNull final XLineBreakpoint<DBBreakpointProperties> breakpoint) throws Exception {
        getManagerThread().invokeAndWait(new DebuggerCommandImpl() {
            @Override
            protected void action() throws Exception {
                //EventRequestManager eventRequestManager = virtualMachineProxy.eventRequestManager();
                DebugProcessImpl jdiDebugProcess = getJdiDebugProcess();
                DBSchemaObject object = getDatabaseObject(breakpoint);
                if (object != null) {
                    VirtualMachineProxyImpl virtualMachineProxy = jdiDebugProcess.getVirtualMachineProxy();
                    RequestManagerImpl requestsManager = jdiDebugProcess.getRequestsManager();

                    String programIdentifier = getProgramIdentifier(breakpoint);

                    LineBreakpoint lineBreakpoint = getLineBreakpoint(getSession().getProject(), breakpoint);
                    List<ReferenceType> referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                    if (referenceTypes.size() > 0) {
                        ReferenceType referenceType = referenceTypes.get(0);
                        List<Location> locations = referenceType.locationsOfLine(breakpoint.getLine() + 1);
                        if (locations != null && locations.size() > 0) {
                            Location location = locations.get(0);
                            BreakpointRequest breakpointRequest = requestsManager.createBreakpointRequest(lineBreakpoint, location);
                            requestsManager.enableRequest(breakpointRequest);

//                            BreakpointRequest breakpointRequest = eventRequestManager.createBreakpointRequest(location);
//                            ThreadReferenceProxyImpl threadReferenceProxy = virtualMachineProxy.allThreads().iterator().next();
//                            ThreadReference threadReference = threadReferenceProxy.getThreadReference();
//
//                            breakpointRequest.addThreadFilter(threadReference);
//                            breakpointRequest.enable();
                        }
                    } else {
                        getDebugProcess().getConsole().warning("Could not resolve breakpoint position: " + DBBreakpointUtil.getBreakpointDesc(breakpoint));
                    }
                }
            }
        });

        return new BreakpointInfo();
    }

    private void doAddBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) {

    }

    @Override
    protected void removeBreakpoint(boolean temporary, Integer breakpointId) throws SQLException {

    }

    @Override
    protected void enableBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) throws Exception {

    }

    @Override
    protected String disableBreakpoint(Integer breakpointId) throws SQLException {
        return null;
    }
}
