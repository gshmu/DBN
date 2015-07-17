package com.dci.intellij.dbn.debugger.jdwp;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.engine.requests.RequestManagerImpl;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.*;

public class DBJdwpBreakpointHandler extends DBBreakpointHandler<DBJdwpDebugProcess> {
    private Set<String> initRequestCache = new HashSet<String>();

    public DBJdwpBreakpointHandler(XDebugSession session, DBJdwpDebugProcess debugProcess) {
        super(session, debugProcess);
        //resetBreakpoints();
    }

    @Override
    public void registerBreakpoints(final Collection<XLineBreakpoint<DBBreakpointProperties>> breakpoints) {
/*        for (XLineBreakpoint<DBBreakpointProperties> breakpoint : breakpoints) {
            initializeResources(breakpoint);
        }
        clearVmCaches();*/

        super.registerBreakpoints(breakpoints);
    }

    void clearVmCaches() {
        getManagerThread().invokeAndWait(new DebuggerCommandImpl() {
            @Override
            protected void action() throws Exception {
                VirtualMachineProxyImpl virtualMachineProxy = getVirtualMachineProxy();
                virtualMachineProxy.clearCaches();
            }
        });
    }

    private void initializeResources(@NotNull final XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        DBSchemaObject object = getDatabaseObject(breakpoint);
        if (object != null) {
            getManagerThread().invokeAndWait(new DebuggerCommandImpl() {
                @Override
                protected void action() throws Exception {
                    Project project = getSession().getProject();
                    String programIdentifier = getProgramIdentifier(breakpoint);
                    boolean initRequested = initRequestCache.contains(programIdentifier);
                    VirtualMachineProxyImpl virtualMachineProxy = getVirtualMachineProxy();
                    List<ReferenceType> referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                    if (!initRequested && referenceTypes.size() == 0) {
                        initRequestCache.add(programIdentifier);
                        //ClassPrepareRequest classPrepareRequest = eventRequestManager.createClassPrepareRequest();
                        //classPrepareRequest.addClassFilter(programIdentifier);
                        //classPrepareRequest.enable();

                        LineBreakpoint lineBreakpoint = getLineBreakpoint(project, breakpoint);
                        RequestManagerImpl requestsManager = getRequestsManager();
                        ClassPrepareRequest classPrepareRequest = requestsManager.createClassPrepareRequest(lineBreakpoint, programIdentifier);
                        classPrepareRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
                        requestsManager.enableRequest(classPrepareRequest);
                    }
                }
            });
        }
    }

    private ThreadReference getMainThread() {
        VirtualMachineProxyImpl virtualMachineProxy = getVirtualMachineProxy();
        ThreadReferenceProxyImpl threadReferenceProxy = virtualMachineProxy.allThreads().iterator().next();
        return threadReferenceProxy.getThreadReference();
    }

    DebuggerManagerThreadImpl getManagerThread() {
        return getJdiDebugProcess().getManagerThread();
    }

    private DebugProcessImpl getJdiDebugProcess() {
        return getDebugProcess().getDebuggerSession().getProcess();
    }

    private RequestManagerImpl getRequestsManager() {
        return getJdiDebugProcess().getRequestsManager();
    }

    private VirtualMachineProxyImpl getVirtualMachineProxy() {
        return getJdiDebugProcess().getVirtualMachineProxy();
    }


    @Override
    public void registerBreakpoint(@NotNull final XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        getManagerThread().invoke(new DebuggerCommandImpl() {
            @Override
            protected void action() throws Exception {
                //EventRequestManager eventRequestManager = virtualMachineProxy.eventRequestManager();

                VirtualMachineProxyImpl virtualMachineProxy = getVirtualMachineProxy();
                RequestManagerImpl requestsManager = getRequestsManager();

                String programIdentifier = getProgramIdentifier(breakpoint);
                LineBreakpoint lineBreakpoint = getLineBreakpoint(getSession().getProject(), breakpoint);
                List<ReferenceType> referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                if (referenceTypes.size() > 0) {
                    ReferenceType referenceType = referenceTypes.get(0);
                    List<Location> locations = referenceType.locationsOfLine(breakpoint.getLine() + 1);
                    if (locations != null && locations.size() > 0) {
                        Location location = locations.get(0);
                        BreakpointRequest breakpointRequest = requestsManager.createBreakpointRequest(lineBreakpoint, location);
                        breakpointRequest.addThreadFilter(getMainThread());
                        requestsManager.enableRequest(breakpointRequest);
                    }
                } else {
                    requestsManager.callbackOnPrepareClasses(lineBreakpoint, programIdentifier);
                }
            }
        });
    }

    @Override
    public void unregisterBreakpoint(@NotNull final XLineBreakpoint<DBBreakpointProperties> breakpoint, final boolean temporary) {
        getManagerThread().invoke(new DebuggerCommandImpl() {
            @Override
            protected void action() throws Exception {
                RequestManagerImpl requestsManager = getRequestsManager();
                LineBreakpoint lineBreakpoint = getLineBreakpoint(getSession().getProject(), breakpoint);
                if (temporary) {
                    final Set<EventRequest> requests = requestsManager.findRequests(lineBreakpoint);
                    for (EventRequest request : requests) {
                        request.disable();
                    }

                } else {
                    requestsManager.deleteRequest(lineBreakpoint);
                }
            }
        });
    }
}
