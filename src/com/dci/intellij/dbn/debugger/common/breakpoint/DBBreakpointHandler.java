package com.dci.intellij.dbn.debugger.common.breakpoint;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.debugger.DBDebugConsoleLogger;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
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

public abstract class DBBreakpointHandler<T extends DBDebugProcess> extends XBreakpointHandler<XLineBreakpoint<DBBreakpointProperties>> {
    public static final Key<Integer> BREAKPOINT_ID_KEY = new Key<Integer>("BREAKPOINT_ID");
    public static final Key<VirtualFile> BREAKPOINT_FILE_KEY = Key.create("DBNavigator.BreakpointFile");

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
                String breakpointDesc = getBreakpointDesc(breakpoint, virtualFile);

                try {
                    if (getBreakpointId(breakpoint) != null) {
                        enableBreakpoint(breakpoint);

                    } else {
                        DBSchemaObject object = getObject(virtualFile);
                        BreakpointInfo breakpointInfo = addBreakpoint(breakpoint, object);

                        String error = breakpointInfo.getError();
                        if (error != null) {
                            session.updateBreakpointPresentation( breakpoint,
                                    Icons.DEBUG_INVALID_BREAKPOINT,
                                    "INVALID: " + error);
                            console.error("Error adding breakpoint: " + breakpointDesc + ". " + error);
                        } else {
                            Integer breakpointId = breakpointInfo.getBreakpointId();
                            breakpoint.putUserData(BREAKPOINT_ID_KEY, breakpointId);

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

        Integer breakpointId = breakpoint.getUserData(BREAKPOINT_ID_KEY);
        if (breakpointId != null) {
            DBDebugConsoleLogger console = debugProcess.getConsole();

            VirtualFile virtualFile = getVirtualFile(breakpoint);
            Project project = session.getProject();
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);
            if (connectionHandler != null && connectionHandler == debugProcess.getConnectionHandler()) {
                String breakpointDesc = getBreakpointDesc(breakpoint, virtualFile);
                try {
                    removeBreakpoint(temporary, breakpointId);
                    console.system("Breakpoint removed: " + breakpointDesc);
                } catch (SQLException e) {
                    console.error("Error removing breakpoint: " + breakpointDesc + ". " + e.getMessage());
                    NotificationUtil.sendErrorNotification(project, "Error.", e.getMessage());
                } finally {
                    breakpoint.putUserData(BREAKPOINT_ID_KEY, null);
                }
            }
        }

    }

    protected abstract BreakpointInfo addBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint, @Nullable DBSchemaObject object) throws Exception;

    protected DatabaseDebuggerInterface getDebuggerInterface(DBSchemaObject object) {
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
                        lineBreakpoint.putUserData(BREAKPOINT_ID_KEY, null);
                    }
                }
            }
        }
    }

    protected Integer getBreakpointId(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        return breakpoint.getUserData(BREAKPOINT_ID_KEY);
    }

    public static VirtualFile getVirtualFile(XLineBreakpoint<DBBreakpointProperties> breakpoint) {
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


    @NotNull
    private String getBreakpointDesc(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint, VirtualFile virtualFile) {
        DBSchemaObject object = getObject(virtualFile);
        int line = breakpoint.getLine() + 1;
        return object == null ?
                virtualFile.getName() + " Line " + line :
                object.getQualifiedName() + " Line " + line;
    }

    @Nullable
    private DBSchemaObject getObject(VirtualFile virtualFile) {
        DBSchemaObject object = null;
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile objectVirtualFile = (DBEditableObjectVirtualFile) virtualFile;
            object = objectVirtualFile.getObject();
        }
        return object;
    }

}
