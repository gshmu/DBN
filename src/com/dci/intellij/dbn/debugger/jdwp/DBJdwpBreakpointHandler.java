package com.dci.intellij.dbn.debugger.jdwp;

import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.engine.requests.RequestManagerImpl;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;

public class DBJdwpBreakpointHandler extends DBBreakpointHandler<DBJdwpDebugProcess> {
    public DBJdwpBreakpointHandler(XDebugSession session, DBJdwpDebugProcess debugProcess) {
        super(session, debugProcess);
        //resetBreakpoints();
    }

    @Override
    protected BreakpointInfo addBreakpoint(@NotNull final XLineBreakpoint<DBBreakpointProperties> breakpoint, @Nullable final DBSchemaObject object) throws Exception {
        final DebugProcessImpl debugProcess = getDebugProcess().getDebuggerSession().getProcess();
        debugProcess.getManagerThread().invokeAndWait(new DebuggerCommandImpl() {
            @Override
            protected void action() throws Exception {
                try {

                    VirtualMachineProxyImpl virtualMachineProxy = debugProcess.getVirtualMachineProxy();
                    RequestManagerImpl requestsManager = debugProcess.getRequestsManager();
                    EventRequestManager eventRequestManager = virtualMachineProxy.eventRequestManager();
                    DBContentType contentType = DBContentType.CODE;
                    VirtualFile virtualFile = getVirtualFile(breakpoint);
                    if (virtualFile instanceof DBSourceCodeVirtualFile) {
                        DBSourceCodeVirtualFile sourceCodeVirtualFile = (DBSourceCodeVirtualFile) virtualFile;
                        contentType = sourceCodeVirtualFile.getContentType();
                    }

                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface(object);
                    String programIdentifier = debuggerInterface.getJdwpProgramIdentifier(object.getObjectType(), contentType, object.getQualifiedName());

                    List<ReferenceType> referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                    if (referenceTypes == null || referenceTypes.size() == 0) {
                        ClassPrepareRequest classPrepareRequest = eventRequestManager.createClassPrepareRequest();
                        classPrepareRequest.addClassFilter(programIdentifier);
                        if (!classPrepareRequest.isEnabled()) {
                            classPrepareRequest.enable();
                        }
                        referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                    }

                    if (referenceTypes != null && referenceTypes.size() > 0) {
                        ReferenceType referenceType = referenceTypes.get(0);
                        List<Location> locations = referenceType.locationsOfLine(breakpoint.getLine() + 1);
                        if (locations != null && locations.size() > 0) {
                            Location location = locations.get(0);
                            BreakpointRequest breakpointRequest = eventRequestManager.createBreakpointRequest(location);

                            ThreadReferenceProxyImpl threadReferenceProxy = virtualMachineProxy.allThreads().iterator().next();
                            ThreadReference threadReference = threadReferenceProxy.getThreadReference();

                            breakpointRequest.addThreadFilter(threadReference);
                            breakpointRequest.enable();
                            Location breakpointLocation = breakpointRequest.location();
                            System.out.println(breakpointRequest);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        return new BreakpointInfo();
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
