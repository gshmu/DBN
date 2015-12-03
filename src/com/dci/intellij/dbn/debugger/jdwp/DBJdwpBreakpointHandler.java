package com.dci.intellij.dbn.debugger.jdwp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.engine.requests.RequestManagerImpl;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.debugger.requests.ClassPrepareRequestor;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.getDatabaseObject;
import static com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUtil.getProgramIdentifier;

public class DBJdwpBreakpointHandler extends DBBreakpointHandler<DBJdwpDebugProcess> {
    private static final Key<LineBreakpoint> LINE_BREAKPOINT_KEY = Key.create("DBNavigator.LineBreakpoint");
    private Set<String> initRequestCache = new HashSet<String>();

    public DBJdwpBreakpointHandler(XDebugSession session, DBJdwpDebugProcess debugProcess) {
        super(session, debugProcess);
    }

    public void registerDefaultBreakpoint(DBMethod method) {
        DBEditableObjectVirtualFile mainDatabaseFile = DBDebugUtil.getMainDatabaseFile(method);
        if (mainDatabaseFile != null) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) mainDatabaseFile.getMainContentFile();
            PSQLFile psqlFile = (PSQLFile) sourceCodeFile.getPsiFile();
            if (psqlFile != null) {
                BasePsiElement basePsiElement = psqlFile.lookupObjectDeclaration(method.getObjectType().getGenericType(), method.getName());
                if (basePsiElement != null) {
                    BasePsiElement subject = basePsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    int offset = subject.getTextOffset();
                    Document document = DocumentUtil.getDocument(psqlFile);
                    int line = document.getLineNumber(offset);

                    DBSchemaObject schemaObject = DBDebugUtil.getMainDatabaseObject(method);
                    if (schemaObject != null) {
                        XDebuggerUtil debuggerUtil = XDebuggerUtil.getInstance();
                        debuggerUtil.toggleLineBreakpoint(getSession().getProject(), sourceCodeFile, line, true);
                    }
                }
            }
        }
    }

    @Override
    public void unregisterDefaultBreakpoint() {

    }

    @Override
    protected void registerDatabaseBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
        new ManagedThreadCommand(getJdiDebugProcess()) {

            @Override
            protected void action() throws Exception {
                //EventRequestManager eventRequestManager = virtualMachineProxy.eventRequestManager();

                VirtualMachineProxyImpl virtualMachineProxy = getVirtualMachineProxy();
                RequestManagerImpl requestsManager = getRequestsManager();

                String programIdentifier = getProgramIdentifier(breakpoint);
                LineBreakpoint lineBreakpoint = getLineBreakpoint(getSession().getProject(), breakpoint);
                if (lineBreakpoint != null) {
                    boolean addBreakpointRequest = true;
                    Set<EventRequest> requests = requestsManager.findRequests(lineBreakpoint);
                    for (EventRequest request : requests) {
                        if (request instanceof BreakpointRequest) {
                            addBreakpointRequest = false;
                        }
                    }

                    if (addBreakpointRequest) {
                        List<ReferenceType> referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                        if (referenceTypes.size() == 0) {
                            requestsManager.callbackOnPrepareClasses(lineBreakpoint, programIdentifier);
                            List list = getJdiDebugProcess().getVirtualMachineProxy().classesByName(programIdentifier);
/*                            for (final Object aList : list) {
                                ReferenceType refType = (ReferenceType)aList;
                                if (refType.isPrepared()) {
                                    processClassPrepare(debugProcess, refType);
                                }
                            }*/
                            referenceTypes = virtualMachineProxy.classesByName(programIdentifier);
                        }

                        if (referenceTypes.size() > 0) {
                            ReferenceType referenceType = referenceTypes.get(0);
                            List<Location> locations = referenceType.locationsOfLine(breakpoint.getLine() + 1);
                            if (locations.size() > 0) {
                                Location location = locations.get(0);
                                BreakpointRequest breakpointRequest = requestsManager.createBreakpointRequest(lineBreakpoint, location);
                                breakpointRequest.addThreadFilter(getMainThread());
                                requestsManager.enableRequest(breakpointRequest);
                            }
                        }
                    }
                }
            }
        }.invoke();
    }

    @Override
    public void prepareObjectClasses(List<XLineBreakpoint<XBreakpointProperties>> breakpoints, List<? extends DBObject> objects) {
        for (DBObject object : objects) {
            if (object instanceof DBSchemaObject) {
                DBSchemaObject schemaObject = (DBSchemaObject) object;
                DBContentType contentType = schemaObject.getContentType();
                if (contentType == DBContentType.CODE) {
                    prepareObjectClasses(schemaObject, DBContentType.CODE);
                } else if (contentType == DBContentType.CODE_SPEC_AND_BODY) {
                    prepareObjectClasses(schemaObject, DBContentType.CODE_SPEC);
                    prepareObjectClasses(schemaObject, DBContentType.CODE_BODY);
                }
            }
        }

        for (XLineBreakpoint<XBreakpointProperties> breakpoint : breakpoints) {
            prepareObjectClasses(breakpoint);
        }
    }

    public void prepareObjectClasses(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
        XBreakpointProperties properties = breakpoint.getProperties();
        if (properties instanceof DBBreakpointProperties) {
            DBBreakpointProperties breakpointProperties = (DBBreakpointProperties) properties;
            if (breakpointProperties.getConnectionHandler() == getConnectionHandler()) {
                new ManagedThreadCommand(getJdiDebugProcess()) {

                    @Override
                    protected void action() throws Exception {
                        RequestManagerImpl requestsManager = getRequestsManager();

                        String programIdentifier = getProgramIdentifier(breakpoint);
                        LineBreakpoint lineBreakpoint = getLineBreakpoint(getSession().getProject(), breakpoint);
                        if (lineBreakpoint != null) {
                            Set<EventRequest> requests = requestsManager.findRequests(lineBreakpoint);
                            if (requests.size() == 0) {
                                ClassPrepareRequest request = requestsManager.createClassPrepareRequest(lineBreakpoint, programIdentifier);
                                if (request != null) {
                                    requestsManager.enableRequest(request);
                                }
                            }
                        }
                    }
                }.invoke();
            }
        }
    }

    public void prepareObjectClasses(DBSchemaObject object, DBContentType contentType) {
        RequestManagerImpl requestsManager = getRequestsManager();
        String programIdentifier = getProgramIdentifier(object, contentType);

        ClassPrepareRequest request = requestsManager.createClassPrepareRequest(new ClassPrepareRequestor() {
            @Override
            public void processClassPrepare(DebugProcess debuggerProcess, ReferenceType referenceType) {
                System.out.println();
            }
        }, programIdentifier);
        if (request != null) {
            requestsManager.enableRequest(request);
        }
    }

    @Override
    protected void unregisterDatabaseBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
        new ManagedThreadCommand(getJdiDebugProcess()) {
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
        }.invoke();
    }

    private void initializeResources(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
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

    @Nullable
    public static LineBreakpoint getLineBreakpoint(Project project, @NotNull XLineBreakpoint breakpoint) {
        LineBreakpoint lineBreakpoint = breakpoint.getUserData(LINE_BREAKPOINT_KEY);
        if (lineBreakpoint == null) {
            lineBreakpoint = LineBreakpoint.create(project, breakpoint);
            breakpoint.putUserData(LINE_BREAKPOINT_KEY, lineBreakpoint);
        }
        return lineBreakpoint;
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
}
