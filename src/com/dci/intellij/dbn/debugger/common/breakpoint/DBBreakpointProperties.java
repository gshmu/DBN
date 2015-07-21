package com.dci.intellij.dbn.debugger.common.breakpoint;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

public class DBBreakpointProperties extends XBreakpointProperties<DBBreakpointState> {
    private VirtualFile file;
    private int line;
    private DBBreakpointState state = new DBBreakpointState(true);

    public DBBreakpointProperties(VirtualFile file, int line) {
        this.file = file;
        this.line = line;
    }

    public VirtualFile getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }


    public DBBreakpointState getState() {
        return state;
    }

    public void loadState(DBBreakpointState state) {
        this.state = state;
    }
}
