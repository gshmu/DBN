package com.dci.intellij.dbn.debugger.jdwp;

import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointHandler;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointProperties;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DBJdwpBreakpointHandler extends DBBreakpointHandler<DBJdwpDebugProcess> {
    public static final Key<Integer> BREAKPOINT_ID_KEY = new Key<Integer>("BREAKPOINT_ID");
    public static final Key<VirtualFile> BREAKPOINT_FILE_KEY = Key.create("DBNavigator.BreakpointFile");

    public DBJdwpBreakpointHandler(XDebugSession session, DBJdwpDebugProcess debugProcess) {
        super(session, debugProcess);
        //resetBreakpoints();
    }

    @Override
    protected BreakpointInfo addBreakpoint(@NotNull XLineBreakpoint<DBBreakpointProperties> breakpoint, DBSchemaObject object) throws Exception {
        return null;
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
