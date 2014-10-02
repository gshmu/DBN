package com.dci.intellij.dbn.execution.compiler;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.vfs.VirtualFile;

public class CompilerAction {
    public static final CompilerAction BULK_COMPILE_ACTION = new CompilerAction(Type.BULK_COMPILE);

    private Type type;
    private VirtualFile virtualFile;
    private int offset;

    public CompilerAction(Type type) {
        this.type = type;
    }

    public CompilerAction(Type type, VirtualFile virtualFile) {
        this.type = type;
        this.virtualFile = virtualFile;
    }

    public CompilerAction(Type type, VirtualFile virtualFile, int offset) {
        this.type = type;
        this.virtualFile = virtualFile;
        this.offset = offset;
    }

    public Type getType() {
        return type;
    }

    @Nullable
    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public int getOffset() {
        return offset;
    }

    public static enum Type {
        SAVE,
        COMPILE,
        BULK_COMPILE,
        DDL
    }
}
