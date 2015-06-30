package com.dci.intellij.dbn.debugger.breakpoint;

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
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

public class DBProgramBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<DBProgramBreakpointProperties>> {
    public static final Key<Integer> BREAKPOINT_ID_KEY = new Key<Integer>("BREAKPOINT_ID");
    public static final Key<VirtualFile> BREAKPOINT_FILE_KEY = Key.create("DBNavigator.BreakpointFile");

    private XDebugSession session;
    private DBProgramDebugProcess debugProcess;

    public DBProgramBreakpointHandler(XDebugSession session, DBProgramDebugProcess debugProcess) {
        super(DBProgramBreakpointType.class);
        this.session = session;
        this.debugProcess  =debugProcess;
        //resetBreakpoints();
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<DBProgramBreakpointProperties> breakpoint) {
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
                DBSchemaObject object = null;
                if (virtualFile instanceof DBEditableObjectVirtualFile) {
                    DBEditableObjectVirtualFile objectVirtualFile = (DBEditableObjectVirtualFile) virtualFile;
                    object = objectVirtualFile.getObject();
                }

                DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();

                Connection debugConnection = debugProcess.getDebugConnection();
                try {
                    Integer breakpointId = breakpoint.getUserData(BREAKPOINT_ID_KEY);

                    if (breakpointId != null) {
                        BreakpointOperationInfo breakpointOperationInfo = debuggerInterface.enableBreakpoint(breakpointId, debugConnection);
                        String error = breakpointOperationInfo.getError();
                        if (error != null) {
                            session.updateBreakpointPresentation( breakpoint,
                                    Icons.DEBUG_INVALID_BREAKPOINT,
                                    "INVALID: " + error);
                        }

                    } else {
                        BreakpointInfo breakpointInfo = object == null ?
                                debuggerInterface.addSourceBreakpoint(
                                        breakpoint.getLine(),
                                        debugConnection) :
                                debuggerInterface.addProgramBreakpoint(
                                        object.getSchema().getName(),
                                        object.getName(),
                                        object.getObjectType().getName().toUpperCase(),
                                        breakpoint.getLine(),
                                        debugConnection);

                        String error = breakpointInfo.getError();
                        if (error != null) {
                            session.updateBreakpointPresentation( breakpoint,
                                    Icons.DEBUG_INVALID_BREAKPOINT,
                                    "INVALID: " + error);
                        } else {
                            breakpoint.putUserData(BREAKPOINT_ID_KEY, breakpointInfo.getBreakpointId());

                            if (!breakpoint.isEnabled()) {
                                BreakpointOperationInfo breakpointOperationInfo = debuggerInterface.disableBreakpoint(breakpointInfo.getBreakpointId(), debugConnection);
                                error = breakpointOperationInfo.getError();
                                if (error != null) {
                                    session.updateBreakpointPresentation( breakpoint,
                                            Icons.DEBUG_INVALID_BREAKPOINT,
                                            "INVALID: " + error);
                                }

                            }
                        }
                    }

                } catch (SQLException e) {
                    session.updateBreakpointPresentation( breakpoint,
                            Icons.DEBUG_INVALID_BREAKPOINT,
                            "INVALID: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<DBProgramBreakpointProperties> breakpoint, boolean temporary) {
        if (!debugProcess.getStatus().CAN_SET_BREAKPOINTS) return;

        Integer breakpointId = breakpoint.getUserData(BREAKPOINT_ID_KEY);
        if (breakpointId != null) {
            VirtualFile virtualFile = getVirtualFile(breakpoint);
            Project project = session.getProject();
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);
            if (connectionHandler != null && connectionHandler == debugProcess.getConnectionHandler()) {
                try {
                    Connection debugConnection = debugProcess.getDebugConnection();
                    DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
                    if (temporary) {
                        debuggerInterface.disableBreakpoint(breakpointId, debugConnection);
                    } else {
                        debuggerInterface.removeBreakpoint(breakpointId, debugConnection);
                    }
                } catch (SQLException e) {
                    NotificationUtil.sendErrorNotification(project, "Error stopping debugger session.", e.getMessage());
                } finally {
                    breakpoint.putUserData(BREAKPOINT_ID_KEY, null);
                }
            }
        }

    }


    private void resetBreakpoints() {
        Project project = session.getProject();

        XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
        XBreakpoint<?>[] breakpoints = breakpointManager.getAllBreakpoints();

        for (XBreakpoint breakpoint : breakpoints) {
            if (breakpoint.getType() instanceof DBProgramBreakpointType) {
                XLineBreakpoint lineBreakpoint = (XLineBreakpoint) breakpoint;
                VirtualFile virtualFile = getVirtualFile(lineBreakpoint);
                if (virtualFile != null) {
                    FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
                    ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);

                    if (connectionHandler == debugProcess.getConnectionHandler()) {
                        lineBreakpoint.putUserData(BREAKPOINT_ID_KEY, null);
                    }
                }
            }
        }
    }

    public static VirtualFile getVirtualFile(XLineBreakpoint<DBProgramBreakpointProperties> breakpoint) {
        VirtualFile breakpointFile = breakpoint.getUserData(BREAKPOINT_FILE_KEY);
        if (breakpointFile == null) {
            DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
            String fileUrl = breakpoint.getFileUrl();
            if (databaseFileSystem.isDatabaseUrl(fileUrl)) {
                VirtualFile virtualFile = databaseFileSystem.findFileByPath(fileUrl);
                if (virtualFile instanceof DBContentVirtualFile) {
                    DBContentVirtualFile contentVirtualFile = (DBContentVirtualFile) virtualFile;
                    breakpointFile = contentVirtualFile.getMainDatabaseFile();
                    breakpoint.putUserData(BREAKPOINT_FILE_KEY, breakpointFile);
                } else if (virtualFile instanceof DBConsoleVirtualFile) {
                    breakpointFile = virtualFile;
                    breakpoint.putUserData(BREAKPOINT_FILE_KEY, breakpointFile);
                }
            } else {
                return VirtualFileManager.getInstance().findFileByUrl(fileUrl);
            }
        }
        return breakpointFile;
    }


}
