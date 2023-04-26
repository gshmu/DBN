package com.dci.intellij.dbn.debugger.common.breakpoint;

import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBBreakpointUtil {
    private static final Key<Integer> BREAKPOINT_ID_KEY = new Key<>("BREAKPOINT_ID");
    private static final Key<VirtualFile> BREAKPOINT_FILE_KEY = Key.create("DBNavigator.BreakpointFile");

    public static Integer getBreakpointId(@NotNull XLineBreakpoint breakpoint) {
        return breakpoint.getUserData(DBBreakpointUtil.BREAKPOINT_ID_KEY);
    }

    public static void setBreakpointId(@NotNull XLineBreakpoint breakpoint, Integer id) {
        breakpoint.putUserData(BREAKPOINT_ID_KEY, id);
    }

    @Nullable
    public static VirtualFile getVirtualFile(@NotNull XLineBreakpoint breakpoint) {
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

    @Nullable
    public static DBSchemaObject getDatabaseObject(@NotNull XLineBreakpoint breakpoint) {
        VirtualFile virtualFile = getVirtualFile(breakpoint);
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile objectVirtualFile = (DBEditableObjectVirtualFile) virtualFile;
            return objectVirtualFile.getObject();
        }
        return null;
    }

    public static DBContentType getContentType(@NotNull XLineBreakpoint breakpoint) {
        DBContentType contentType = DBContentType.CODE;
        VirtualFile virtualFile = getVirtualFile(breakpoint);
        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            contentType = sourceCodeFile.getContentType();
        }
        return contentType;
    }

    @Nullable
    public static String getProgramIdentifier(@NotNull ConnectionHandler connection, @NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
        DBSchemaObject object = getDatabaseObject(breakpoint);
        DBContentType contentType = getContentType(breakpoint);
        return getProgramIdentifier(connection, object, contentType);
    }

    @Nullable
    public static String getProgramIdentifier(@NotNull ConnectionHandler connection, DBSchemaObject object, DBContentType contentType) {
        DatabaseDebuggerInterface debuggerInterface = connection.getDebuggerInterface();
        return object == null ?
                debuggerInterface.getJdwpBlockIdentifier() :
                debuggerInterface.getJdwpProgramIdentifier(object.getObjectType(), contentType, object.getQualifiedName());
    }

    @NotNull
    public static String getBreakpointDesc(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
        DBSchemaObject object = getDatabaseObject(breakpoint);
        VirtualFile virtualFile = getVirtualFile(breakpoint);
        int line = breakpoint.getLine() + 1;
        Integer breakpointId = getBreakpointId(breakpoint);
        String base = object == null ?
                virtualFile == null ? "unknown" : virtualFile.getName():
                object.getQualifiedName();

        return base + ":" + line + " (id=" + breakpointId + ")";
    }

    public static List<XLineBreakpoint<XBreakpointProperties>> getDatabaseBreakpoints(final ConnectionHandler connection) {
        return Read.call(connection, c -> {
            DBBreakpointType databaseBreakpointType = XDebuggerUtil.getInstance().findBreakpointType(DBBreakpointType.class);
            Project project = c.getProject();
            XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
            Collection<XLineBreakpoint<XBreakpointProperties>> breakpoints =
                    (Collection<XLineBreakpoint<XBreakpointProperties>>) breakpointManager.getBreakpoints(databaseBreakpointType);

            List<XLineBreakpoint<XBreakpointProperties>> connectionBreakpoints = new ArrayList<>();
            for (XLineBreakpoint<XBreakpointProperties> breakpoint : breakpoints) {
                XBreakpointProperties properties = breakpoint.getProperties();
                if (properties instanceof DBBreakpointProperties) {
                    DBBreakpointProperties breakpointProperties = (DBBreakpointProperties) properties;
                    if (c == breakpointProperties.getConnection()) {
                        connectionBreakpoints.add(breakpoint);
                    }
                }
            }
            return connectionBreakpoints;
        });
    }
}
