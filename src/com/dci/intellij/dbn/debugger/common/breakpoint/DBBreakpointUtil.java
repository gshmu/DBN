package com.dci.intellij.dbn.debugger.common.breakpoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

public class DBBreakpointUtil {
    private static final Key<Integer> BREAKPOINT_ID_KEY = new Key<Integer>("BREAKPOINT_ID");
    private static final Key<VirtualFile> BREAKPOINT_FILE_KEY = Key.create("DBNavigator.BreakpointFile");
    private static final Key<LineBreakpoint> LINE_BREAKPOINT_KEY = Key.create("DBNavigator.LineBreakpoint");


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
    @NotNull
    public static LineBreakpoint getLineBreakpoint(Project project, @NotNull XLineBreakpoint breakpoint) {
        LineBreakpoint lineBreakpoint = breakpoint.getUserData(LINE_BREAKPOINT_KEY);
        if (lineBreakpoint == null) {
            lineBreakpoint = LineBreakpoint.create(project, breakpoint);
            breakpoint.putUserData(LINE_BREAKPOINT_KEY, lineBreakpoint);
        }
        return lineBreakpoint;
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
            DBSourceCodeVirtualFile sourceCodeVirtualFile = (DBSourceCodeVirtualFile) virtualFile;
            contentType = sourceCodeVirtualFile.getContentType();
        }
        return contentType;
    }

    public static String getProgramIdentifier(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        DBSchemaObject object = getDatabaseObject(breakpoint);
        if (object != null) {
            DatabaseDebuggerInterface debuggerInterface = object.getConnectionHandler().getInterfaceProvider().getDebuggerInterface();
            DBContentType contentType = getContentType(breakpoint);
            return debuggerInterface.getJdwpProgramIdentifier(object.getObjectType(), contentType, object.getQualifiedName());
        }
        return null;
    }

    @NotNull
    public static String getBreakpointDesc(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint) {
        DBSchemaObject object = getDatabaseObject(breakpoint);
        final VirtualFile virtualFile = getVirtualFile(breakpoint);
        return object == null ?
                virtualFile == null ? "unknown" : virtualFile.getName() + " Line " + (breakpoint.getLine() + 1) :
                object.getQualifiedName() + " Line " + (breakpoint.getLine() + 1);
    }

}
